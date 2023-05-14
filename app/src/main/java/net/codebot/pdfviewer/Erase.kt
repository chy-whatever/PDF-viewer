package net.codebot.pdfviewer


class Erase(val pageNum: Int) : draw_and_earse {
    var pen_idx: ArrayList<Int>
    var highlighter_idx: ArrayList<Int>
    var weather_earse = true





    override fun is_path(): Boolean { return false; }

    override fun is_earse(): Boolean {
        return true
    }
    fun get_PageNum () : Int { return pageNum; }
    fun get_Pen_idx (): ArrayList<Int> { return pen_idx;}
    fun get_Highlighter_idx(): ArrayList<Int> { return highlighter_idx; }

    init {
        pen_idx = ArrayList()
        highlighter_idx = ArrayList()
    }
}