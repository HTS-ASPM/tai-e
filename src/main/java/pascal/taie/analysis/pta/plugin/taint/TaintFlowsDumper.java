/*
 * Tai-e: A Static Analysis Framework for Java
 *
 * Copyright (C) 2022 Tian Tan <tiantan@nju.edu.cn>
 * Copyright (C) 2022 Yue Li <yueli@nju.edu.cn>
 *
 * This file is part of Tai-e.
 *
 * Tai-e is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * Tai-e is distributed in the hope that it will be useful,but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Tai-e. If not, see <https://www.gnu.org/licenses/>.
 */

package pascal.taie.analysis.pta.plugin.taint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import pascal.taie.ir.stmt.Stmt;
import pascal.taie.language.classes.JMethod;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes the detected taint flows as JSON, for tools that consume them
 * programmatically rather than parsing the log or the taint flow graph.
 *
 * <pre>
 * {"version": 1, "flows": [{
 *   "source": {"kind": "call" | "param" | "field", "method": "&lt;signature&gt;",
 *              "container": "&lt;signature&gt;", "class": "a.b.C", "line": 12, "index": "result"},
 *   "sink":   {"method": "&lt;signature&gt;", "container": "&lt;signature&gt;",
 *              "class": "a.b.C", "line": 30, "index": "0"}}]}
 * </pre>
 * "method" is the source or sink method from the taint config (for a field
 * source, the field); "container" is the method the flow starts or ends in,
 * and "class" its declaring class. "line" is -1 when unknown.
 */
class TaintFlowsDumper {

    static final int VERSION = 1;

    void dump(Set<TaintFlow> flows, File output) throws IOException {
        List<Map<String, Object>> out = new ArrayList<>();
        for (TaintFlow flow : flows) {
            Map<String, Object> f = new LinkedHashMap<>();
            f.put("source", source(flow.sourcePoint()));
            f.put("sink", sink(flow.sinkPoint()));
            out.add(f);
        }
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", VERSION);
        root.put("flows", out);
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
                .writeValue(output, root);
    }

    private static Map<String, Object> source(SourcePoint sp) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (sp instanceof CallSourcePoint csp) {
            m.put("kind", "call");
            m.put("method", csp.source().method().getSignature());
            where(m, csp.getContainer(), csp.sourceCall());
            m.put("index", csp.indexRef().toString());
        } else if (sp instanceof ParamSourcePoint psp) {
            m.put("kind", "param");
            m.put("method", psp.source().method().getSignature());
            where(m, psp.getContainer(), null);
            m.put("index", psp.indexRef().toString());
        } else if (sp instanceof FieldSourcePoint fsp) {
            m.put("kind", "field");
            m.put("method", fsp.source().field().getSignature());
            where(m, fsp.getContainer(), fsp.loadField());
            m.put("index", "result");
        } else {
            throw new IllegalArgumentException("unknown source point: " + sp);
        }
        return m;
    }

    private static Map<String, Object> sink(SinkPoint sp) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("method", sp.sink().method().getSignature());
        where(m, sp.sinkCall().getContainer(), sp.sinkCall());
        m.put("index", sp.indexRef().toString());
        return m;
    }

    private static void where(Map<String, Object> m, JMethod container, Stmt stmt) {
        m.put("container", container.getSignature());
        m.put("class", container.getDeclaringClass().getName());
        m.put("line", stmt == null ? -1 : stmt.getLineNumber());
    }
}
