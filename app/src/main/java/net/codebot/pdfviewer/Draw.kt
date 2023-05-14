package net.codebot.pdfviewer

interface draw_and_earse {

    fun is_path(): Boolean
    fun is_earse(): Boolean
}


class Draw() : draw_and_earse {
    var is_black: Boolean? = null
    var page: Int? = null
    var page_index: Int? = null

    fun make_Draw(b: Boolean?, i: Int, j: Int) {
        this.is_black = b
        this.page_index = j
        this.page = i
    }

    override fun is_path(): Boolean {
        return true
    }

    override fun is_earse(): Boolean {
        return false
    }

    fun is_Pen(): Boolean? {
        return is_black
    }


    fun get_page(): Int? {
        return page
    }





}