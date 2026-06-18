package org.d1scw0rld.bookbag

import org.junit.runners.BlockJUnit4ClassRunner
import org.junit.runners.model.FrameworkMethod
import org.junit.runner.Description
import org.robolectric.RobolectricTestRunner

class DisplayNameRunner(clazz: Class<*>) : BlockJUnit4ClassRunner(clazz) {
    override fun describeChild(method: FrameworkMethod): Description {
        val annotation = method.annotations.find { it.annotationClass.simpleName == "DisplayName" }
        val value = annotation?.let { ann ->
            val valueMethod = ann.annotationClass.java.getMethod("value")
            valueMethod.invoke(ann) as String
        }
        return if (value != null) {
            Description.createTestDescription(
                testClass.javaClass,
                value
            )
        } else {
            super.describeChild(method)
        }
    }
}

class DisplayNameRobolectricRunner(clazz: Class<*>) : RobolectricTestRunner(clazz) {
    override fun describeChild(method: FrameworkMethod): Description {
        val annotation = method.annotations.find { it.annotationClass.simpleName == "DisplayName" }
        val value = annotation?.let { ann ->
            val valueMethod = ann.annotationClass.java.getMethod("value")
            valueMethod.invoke(ann) as String
        }
        return if (value != null) {
            Description.createTestDescription(
                testClass.javaClass,
                value
            )
        } else {
            super.describeChild(method)
        }
    }
}
