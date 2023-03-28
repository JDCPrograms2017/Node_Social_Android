package com.example.nodesocialandroid

class User {
    // usually in Kotlin we use a data class, but Firebase wants an empty constructor
    var name: String? = null
    var email: String? = null
    var uid: String? = null

    constructor(){}

    constructor(name: String?, email: String?, uid: String?) {
        this.name = name
        this.email = email
        this.uid = uid
    }
}