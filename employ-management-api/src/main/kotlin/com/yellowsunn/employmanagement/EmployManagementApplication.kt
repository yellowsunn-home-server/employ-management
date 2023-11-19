package com.yellowsunn.employmanagement

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EmployManagementApplication

fun main(args: Array<String>) {
    println("helo")
    runApplication<EmployManagementApplication>(*args)
}
