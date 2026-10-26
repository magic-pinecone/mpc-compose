package org.mpc.presentation.formatting

import de.infix.testBalloon.framework.core.testSuite
import org.mpc.domain.model.CourseType
import kotlin.test.assertEquals
import kotlin.test.assertNull

val courseTypeLabelTests by testSuite {
    test("required and elective courses are labelled") {
        assertEquals("必修", CourseType.REQUIRED.displayLabel)
        assertEquals("選修", CourseType.ELECTIVE.displayLabel)
    }

    test("an unknown course type has no label instead of 未知") {
        assertNull(CourseType.UNKNOWN.displayLabel)
    }
}
