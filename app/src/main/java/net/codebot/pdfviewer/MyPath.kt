package net.codebot.pdfviewer

import android.graphics.Path

class MyPath(var path: Path) {

    var exits: Boolean = true


    fun get_path(): Path? {
        return path
    }


    fun set_exist(b : Boolean){
        exits = b
    }

}