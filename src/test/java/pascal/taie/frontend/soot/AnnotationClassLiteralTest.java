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


package pascal.taie.frontend.soot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pascal.taie.Main;
import pascal.taie.World;
import pascal.taie.language.annotation.Annotation;
import pascal.taie.language.annotation.ClassElement;
import pascal.taie.language.classes.JClass;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AnnotationClassLiteralTest {

    @BeforeEach
    void setUp() {
        World.reset();
    }

    /**
     * {@code @Returns(void.class)} is stored as the class_info "V"
     * (JVM Spec 4.7.16.1). The Soot frontend used to reject it as an invalid
     * descriptor, which aborted building the whole world, e.g. for Android apps
     * whose libraries carry such annotations.
     */
    @Test
    void voidClassLiteralInAnnotation() {
        Main.buildWorld(
                "-java", "8",
                "-cp", "src/test/resources/world",
                "--input-classes", "VoidClassLiteral",
                "--world-builder", SootWorldBuilder.class.getName());

        JClass c = World.get().getClassHierarchy().getClass("VoidClassLiteral");
        assertNotNull(c);
        Annotation returns = c.getAnnotation("VoidClassLiteral$Returns");
        assertNotNull(returns);
        assertEquals("void", ((ClassElement) returns.getElement("value")).classDescriptor());
    }
}
