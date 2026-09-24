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

package pascal.taie.analysis.pta;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import pascal.taie.Main;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * taint-flows.json holds the same flows the analysis logs, with sources and
 * sinks as signatures, for tools that consume them programmatically.
 */
public class TaintFlowsJsonTest {

    @Test
    void dumpsEveryFlowWithSourceAndSink(@TempDir Path out) throws Exception {
        Main.main(
                "-cp", "src/test/resources/pta/taint",
                "-m", "SimpleTaint",
                "--output-dir", out.toString(),
                "-a", "pta=implicit-entries:false;only-app:true;distinguish-string-constants:all;"
                        + "taint-config:src/test/resources/pta/taint/taint-config.yml");

        JsonNode root = new ObjectMapper().readTree(new File(out.toFile(), "taint-flows.json"));
        assertEquals(1, root.get("version").asInt());
        JsonNode flows = root.get("flows");
        // SimpleTaint-pta-expected.txt: 6 flows, 4 from calls and 2 from fields
        assertEquals(6, flows.size());
        List<String> kinds = new ArrayList<>();
        for (JsonNode f : flows) {
            kinds.add(f.get("source").get("kind").asText());
            assertTrue(f.get("sink").get("method").asText().startsWith("<SourceSink: void sink("));
            assertEquals("SimpleTaint", f.get("sink").get("class").asText());
            assertTrue(f.get("sink").get("line").asInt() > 0);
        }
        assertEquals(4, kinds.stream().filter("call"::equals).count());
        assertEquals(2, kinds.stream().filter("field"::equals).count());
        JsonNode first = flows.get(0).get("source");
        assertEquals("<SourceSink: java.lang.String source()>", first.get("method").asText());
        assertEquals("result", first.get("index").asText());
        assertEquals(4, first.get("line").asInt());
    }
}
