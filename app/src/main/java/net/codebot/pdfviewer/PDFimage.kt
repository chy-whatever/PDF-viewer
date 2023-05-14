package net.codebot.pdfviewer

import java.lang.Math.*
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.ImageView
import android.content.Context
import android.graphics.*
import java.util.*

@SuppressLint("AppCompatCustomView")
class PDFimage   (context: Context?) : ImageView(context) {

    private var translate_x = 0f
    private  var translate_y = 0f

    private var current_page = 0


    var path: Path? = null
    var pen_paths: ArrayList<ArrayList<MyPath>>? = null
    var highlighter_paths: ArrayList<ArrayList<MyPath>>? = null

    var draw_pen: Boolean? = null
    var draw_highlignt:Boolean? = null
    var erase_or_not:Boolean? = null
    var mouse:Boolean? = null


    var bitmap: Bitmap? = null
    var pen_paint: Paint? = null
    var highlighter_paint:Paint? = null

    private var mouse_x = 0f
    private  var mouse_y  = 0f
    var old_a = 0f
    var old_b = 0f
    var old_a_1 = 0f
    var old_b_1 = 0f
    var a = 0f
    var a_1 = 0f
    var b = 0f
    var b_1 = 0f

//    var p1_index = 0
//    var p2_id = 0
//    var p2_index = 0

    var matrix1 = Matrix()
    var matrix2 = Matrix()
    var list_undo: Stack<draw_and_earse>? = null
    var list_redo: Stack<draw_and_earse>? = null
    private var f_array: FloatArray? = null


    fun my_initial(){
        draw_pen = false
        draw_highlignt = false
        erase_or_not = false
        mouse = true
        ini_highlighter()
        ini_paint()
    }

    fun ini_paint(){
        pen_paint = Paint()
        pen_paint!!.strokeWidth = 5f
        pen_paint!!.color = Color.BLACK
        pen_paint!!.style = Paint.Style.STROKE
    }
    fun ini_highlighter(){
        highlighter_paint = Paint()
        highlighter_paint!!.strokeWidth = 30f
        highlighter_paint!!.color = Color.YELLOW
        highlighter_paint!!.style = Paint.Style.STROKE
    }

    fun make_PDFimage(num: Int) {
        my_initial()
        list_redo = Stack<draw_and_earse>()
        list_undo = Stack<draw_and_earse>()
        pen_paths = ArrayList<ArrayList<MyPath>>()
        highlighter_paths = ArrayList<ArrayList<MyPath>>()
        var i = 0;
        while (i < num) {
            pen_paths!!.add(ArrayList<MyPath>())
            highlighter_paths!!.add(ArrayList<MyPath>())
            i++
        }
    }


    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.pointerCount) {

            1 -> when (event.action) {

                MotionEvent.ACTION_MOVE -> {
                    if (mouse!!) {
                        translate_x += event.x - mouse_x
                        translate_y += event.y - mouse_y
                        mouse_x = event.x
                        mouse_y = event.y
                    }
                    var temp_array = floatArrayOf(event.x, 0f)
                    matrix1.invert(matrix2)
                    matrix2.mapPoints(temp_array)
                    val ret_x = temp_array[0] - translate_x
                    var temp_array1 = floatArrayOf(0f, event.y)
                    matrix1.invert(matrix2)
                    matrix2.mapPoints(temp_array1)
                    val ret_y = temp_array1[1] - translate_y

                    path!!.lineTo(ret_x, ret_y)
                }
                MotionEvent.ACTION_DOWN -> {
                    if (mouse!!) {
                        mouse_x = event.x
                        mouse_y  = event.y
                    }

                    path = Path()
                    var temp_array = floatArrayOf(event.x, 0f)
                    matrix1.invert(matrix2)
                    matrix2.mapPoints(temp_array)
                    val ret_x = temp_array[0] - translate_x

                    var temp_array2 = floatArrayOf(0f, event.y)
                    matrix1.invert(matrix2)
                    matrix2.mapPoints(temp_array2)
                    val ret_y = temp_array2[1] - translate_y

                    path!!.moveTo(ret_x,ret_y)
                }
                MotionEvent.ACTION_UP -> {
                    if (draw_pen!!) {
                        pen_paths!![current_page].add(MyPath(path!!))
                        var temp = Draw()
                        temp.make_Draw(true, current_page, pen_paths!![current_page].size - 1)
                        my_add(temp)
                    } else if (draw_highlignt!!) {
                        highlighter_paths!![current_page].add(MyPath(path!!))
                        var temp = Draw()
                        temp.make_Draw(false, current_page, highlighter_paths!![current_page].size - 1)
                        my_add(temp)
                    } else if (erase_or_not!!) {
                        val era = Erase(current_page)
                        my_erase(path, highlighter_paths!![current_page], era, false)
                        my_erase(path, pen_paths!![current_page], era, true)
                        if(era.weather_earse){

                        }else {
                            my_add(era)
                        }
                    }
                    path = null
                }
            }

            2 -> {
                var p1_id = event.getPointerId(0)
                var p1_index = event.findPointerIndex(p1_id)
                f_array = floatArrayOf(event.getX(p1_index), event.getY(p1_index))
                matrix2.mapPoints(f_array)
                store_zoom()
                var p2_id = event.getPointerId(1)
                var p2_index = event.findPointerIndex(p2_id)
                f_array = floatArrayOf(event.getX(p2_index), event.getY(p2_index))
                matrix2.mapPoints(f_array)


                given_old()



                var scale = ret_scale()
                // pan and zoom during MOVE event
                if (event.action == MotionEvent.ACTION_MOVE) {
                    // pan
                    var a_mid = (a + a_1) / 2
                    var b_mid = (b + b_1) / 2

                    var temp_a = a_mid - (old_a + old_a_1) / 2
                    var temp_b = b_mid -(old_b + old_b_1) / 2
                    matrix1.preTranslate( temp_a, temp_b)

                    // zoom
                    scale = max(0f, scale)
                    val point = FloatArray(9)
                    matrix1.getValues(point)
                    matrix1.preScale(scale, scale,  a_mid, b_mid)

                }

            }
        }
        return true
    }

    fun given_old(){
        if (old_a_1 < 0 || old_b_1 < 0) {
            a_1 = f_array!![0]
            old_a_1 = a_1
            b_1 = f_array!![1]
            old_b_1 = b_1
        } else {
            old_a_1 = a_1
            old_b_1 = b_1
            a_1 = f_array!![0]
            b_1 = f_array!![1]
        }
    }

    fun store_zoom(){
        if (old_a < 0 || old_b < 0) {
            a = f_array!![0]
            old_a = a
            b = f_array!![1]
            old_b = b
        } else {
            old_a = a
            old_b = b
            a = f_array!![0]
            b = f_array!![1]
        }
    }

    fun ret_scale(): Float {
        val old = sqrt(pow( (old_a - old_a_1).toDouble(), 2.0) + pow((old_b - old_b_1).toDouble(), 2.0)).toFloat()
        val new = sqrt(pow((a - a_1).toDouble(), 2.0) + pow( (b - b_1).toDouble(), 2.0  ) ).toFloat()
        var scale = new / old
        return scale
    }

    fun init_scale() {
        translate_x = 0f
        translate_y = 0f
        reset_matrix()
    }
    fun reset_matrix(){
        matrix1 = Matrix()
    }

    fun rotate_translate_scale() {
        translate_x = -600f
        translate_y = -300f
        matrix1 = Matrix()
        matrix1.preTranslate(-100f, translate_y)
        matrix1.setScale(1.5f,1.5f)
    }

    fun my_add(a: draw_and_earse) {
        list_undo!!.push(a)
        list_redo = Stack<draw_and_earse>()
        if (list_undo!!.size >= 10) {
            val my_new: Stack<draw_and_earse> = Stack<draw_and_earse>()
            val my_temp: Stack<draw_and_earse> = Stack<draw_and_earse>()
            var inde = 0
            while (inde < 5) {
                my_temp.push(list_undo!!.peek())
                list_undo!!.pop()
                inde++
            }
            while (!my_temp.empty()) {
                my_new.push(my_temp.peek())
                my_temp.pop()
            }
            list_undo = my_new
        }
    }

    // set image as background
    fun setImage(bitmap: Bitmap?, p: Int) {
        translate_x = 0f
        translate_y = 0f
        matrix1 = Matrix()
        current_page = p
        this.bitmap = bitmap
    }




    fun my_undo() {
        if (list_undo!!.empty()) return
        val operation: draw_and_earse = list_undo!!.peek()
        list_undo!!.pop()
        list_redo!!.push(operation)

        if (!operation.is_path()) {
            var i = 0
            val earse_object: Erase = operation as Erase
            val temp: ArrayList<Int> = earse_object.get_Pen_idx()
            var len_1 = temp.size
            val index = earse_object.get_PageNum()
            //op_page = undo_pageNum
            while (i < len_1) {
                pen_paths!![index][temp[i]].set_exist(true)
                i++
            }
            i = 0
            val temp2: ArrayList<Int> = earse_object.get_Highlighter_idx()
            val len_2 = temp2.size
            while (i < len_2) {
                highlighter_paths!![index][temp2[i]].set_exist(true)
                i++
            }
        }
        if (operation.is_path()) {
            val path: Draw = operation as Draw

            if (path.is_Pen()!!) {
                pen_paths!![path.get_page()!!][path.page_index!!].set_exist(false)
            } else {
                highlighter_paths!![path.get_page()!!][path.page_index!!].set_exist(false)
            }
        }

    }

    fun my_redo() {
        if (list_redo!!.empty()) return
        val operation: draw_and_earse = list_redo!!.peek()
        list_redo!!.pop()
        list_undo!!.push(operation)

        if (!operation.is_path()) { // undo erase
            val undo_object: Erase = operation as Erase
            var len_1 = undo_object.get_Pen_idx().size
            val temp: ArrayList<Int> = undo_object.get_Pen_idx()
            val index =undo_object.get_PageNum()
            var i = 0
            while (i < len_1) {
                pen_paths!![index][temp[i]].set_exist(false)
                i++
            }
            i = 0
            var len_2 = undo_object.get_Highlighter_idx().size
            val temp2: ArrayList<Int> = undo_object.get_Highlighter_idx()
            while (i < len_2) {
                highlighter_paths!![index][temp2[i]].set_exist(false)
                i++
            }
        }
        if (operation.is_path()) {
            val path: Draw = operation as Draw
            if (path.is_Pen()!!) {
                pen_paths!![path.get_page()!!][path.page_index!!].exits = true
            } else {
                highlighter_paths!![path.get_page()!!][path.page_index!!].exits = true
            }
        }

    }

    fun change_matrix(){
        matrix1.postTranslate(translate_x, translate_y)
    }
    fun change_matrix_inverse(){
        matrix1.postTranslate(-translate_x, -translate_y)
    }
    override fun onDraw(canvas: Canvas) {
        canvas.save()

        print(matrix1)
        change_matrix()
        canvas.setMatrix(matrix1)


        if (bitmap != null) {
            setImageBitmap(bitmap)
        }
        change_matrix_inverse()

        for (path in highlighter_paths!![current_page]) {
            if (path.exits!!) {
                canvas.drawPath(path.get_path()!!, highlighter_paint!!)
            }
        }

        if (draw_highlignt!! && path != null) {
            canvas.drawPath(path!!, highlighter_paint!!)
        } else if (draw_pen!! && path != null) {
            canvas.drawPath(path!!, pen_paint!!)
        }


        for (path in pen_paths!![current_page]) {
            if (path.exits!!) {
                canvas.drawPath(path.get_path()!!, pen_paint!!)
            }
        }
        super.onDraw(canvas)
        canvas.restore()
    }

    private fun checkpoint(p: Path?): ArrayList<ArrayList<Float>> {
        val temp = PathMeasure(p, false)
        val ret = ArrayList<ArrayList<Float>>()
        val length = temp.length
        var speed = length / 20
        var distance = 0f
        val temp_array = FloatArray(2)
        var counter = 0
        while (distance < length && counter < 20) {
            temp.getPosTan(distance, temp_array, null)
            ret.add(ArrayList())
            ret[counter].add(temp_array[0])
            ret[counter].add(temp_array[1])
            counter++
            distance = distance + speed
        }
        return ret
    }

    private fun my_erase(erase_path: Path?, all: ArrayList<MyPath>, e: Erase, is_pen: Boolean) {
        val len = all.size
        var index = 0
        while (index < len) {
            var bol = erase_intersect(erase_path, all[index].get_path()!!)
            if (bol) {
                e.weather_earse = false
                all[index].set_exist(false)
                if (!is_pen) {
                    e.highlighter_idx.add(index)
                } else {
                    e.pen_idx.add(index)
                }
                return
            }
            index++
        }
    }




    private fun erase_intersect(p1: Path?, p2: Path): Boolean {
        val p1_path = checkpoint(p1)
        val p2_path = checkpoint(p2)
        val len1 = p1_path.size
        val len2 = p2_path.size
        var i = 0
        while (i < len1) {
            var j = 0
            while (j < len2) {
                var a = p2_path[i][0] - p1_path[j][0]
                var b = p2_path[i][1] - p1_path[j][1]
                if ((a <= 20 && a >= -20 ) &&
                    (b <= 20 && b >= -20 ))
                 {
                    return true
                }
                j++
            }
            i++
        }
        return false
    }



}