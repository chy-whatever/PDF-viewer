package net.codebot.pdfviewer

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


// PDF sample code from
// https://medium.com/@chahat.jain0/rendering-a-pdf-document-in-android-activity-fragment-using-pdfrenderer-442462cb8f9a
// Issues about cache etc. are not at all obvious from documentation, so we should expect people to need this.
// We may wish to provide this code.
class MainActivity : AppCompatActivity() {
    val LOGNAME = "pdf_viewer"
    val FILENAME = "shannon1948.pdf"
    val FILERESID = R.raw.shannon1948

    // manage the pages of the PDF, see below
    lateinit var pdfRenderer: PdfRenderer
    lateinit var parcelFileDescriptor: ParcelFileDescriptor
    private var currentPage: PdfRenderer.Page? = null

    private var pdf: LinearLayout? = null
    private var prev_but: Button? = null
    private  var next_but:Button? = null
    private var undo_but: Button? = null
    private  var redo_but:Button? = null
    private  var pen_but:ImageButton? = null
    private  var highlighter_but:ImageButton? = null
    private  var eraser_but:ImageButton? = null
    private  var mouse:ImageButton? = null
    var current_page: TextView? = null


    // custom ImageView class that captures strokes and draws them over the image
    lateinit var pageImage: PDFimage

    fun start_cur(){
        var point = getResources().getDrawable(R.drawable.click)
        var point_s = getResources().getDrawable(R.drawable.click_s)
        var pe=getResources().getDrawable(R.drawable.pe)
        var pen_s=getResources().getDrawable(R.drawable.pen_s)
        var high=getResources().getDrawable(R.drawable.high)
        var high_s=getResources().getDrawable(R.drawable.highlighter_s)
        var earse=getResources().getDrawable(R.drawable.ea)
        var earse_s=getResources().getDrawable(R.drawable.ea_s)
        mouse!!.setOnClickListener(View.OnClickListener { a: View? ->

            pen_but!!.setSelected(false)
            highlighter_but!!.setSelected(false)
            eraser_but!!.setSelected(false)
            mouse!!.setSelected(true)
            pen_but!!.background = pe
            highlighter_but!!.background = high
            eraser_but!!.background = earse
            mouse!!.background = point_s
            pageImage!!.erase_or_not = false
            pageImage!!.draw_pen = false
            pageImage!!.draw_highlignt = false
            pageImage!!.mouse = true
        })
    }

    fun start_pen(){
        var point = getResources().getDrawable(R.drawable.click)
        var point_s = getResources().getDrawable(R.drawable.click_s)
        var pe=getResources().getDrawable(R.drawable.pe)
        var pen_s=getResources().getDrawable(R.drawable.pen_s)
        var high=getResources().getDrawable(R.drawable.high)
        var high_s=getResources().getDrawable(R.drawable.highlighter_s)
        var earse=getResources().getDrawable(R.drawable.ea)
        var earse_s=getResources().getDrawable(R.drawable.ea_s)
        pen_but?.setOnClickListener(View.OnClickListener { a: View? ->
            pen_but!!.setSelected(true)
            highlighter_but!!.setSelected(false)
            eraser_but!!.setSelected(false)
            mouse!!.setSelected(false)
            pen_but!!.background = pen_s
            highlighter_but!!.background = high
            eraser_but!!.background = earse
            mouse!!.background = point
            pageImage!!.erase_or_not = false
            pageImage!!.draw_pen = true
            pageImage!!.draw_highlignt = false
            pageImage!!.mouse = false
        })
    }

    fun start_high_light(){
        var point = getResources().getDrawable(R.drawable.click)
        var point_s = getResources().getDrawable(R.drawable.click_s)
        var pe=getResources().getDrawable(R.drawable.pe)
        var pen_s=getResources().getDrawable(R.drawable.pen_s)
        var high=getResources().getDrawable(R.drawable.high)
        var high_s=getResources().getDrawable(R.drawable.highlighter_s)
        var earse=getResources().getDrawable(R.drawable.ea)
        var earse_s=getResources().getDrawable(R.drawable.ea_s)
        highlighter_but!!.setOnClickListener(View.OnClickListener { a: View? ->
            pen_but!!.setSelected(false)
            highlighter_but!!.setSelected(true)
            eraser_but!!.setSelected(false)
            mouse!!.setSelected(false)
            pen_but!!.background = pe
            highlighter_but!!.background = high_s
            eraser_but!!.background = earse
            mouse!!.background = point
            pageImage!!.erase_or_not = false
            pageImage!!.draw_pen = false
            pageImage!!.draw_highlignt = true
            pageImage!!.mouse = false
        })
    }

    fun start_erase(){
        var point = getResources().getDrawable(R.drawable.click)
        var point_s = getResources().getDrawable(R.drawable.click_s)
        var pe=getResources().getDrawable(R.drawable.pe)
        var pen_s=getResources().getDrawable(R.drawable.pen_s)
        var high=getResources().getDrawable(R.drawable.high)
        var high_s=getResources().getDrawable(R.drawable.highlighter_s)
        var earse=getResources().getDrawable(R.drawable.ea)
        var earse_s=getResources().getDrawable(R.drawable.ea_s)
        eraser_but!!.setOnClickListener(View.OnClickListener { a: View? ->

            pen_but!!.setSelected(false)
            highlighter_but!!.setSelected(false)
            eraser_but!!.setSelected(true)
            mouse!!.setSelected(false)
            pen_but!!.background = pe
            highlighter_but!!.background = high
            eraser_but!!.background = earse_s
            mouse!!.background = point
            pageImage!!.erase_or_not = true
            pageImage!!.draw_pen = false
            pageImage!!.draw_highlignt = false
            pageImage!!.mouse = false
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pen_but = findViewById(R.id.pen)
        highlighter_but = findViewById(R.id.highlighter)
        mouse = findViewById(R.id.mouse)
        mouse?.isSelected = true
        eraser_but = findViewById(R.id.eraser)
        undo_but = findViewById(R.id.undo)
        redo_but = findViewById(R.id.redo)
        prev_but = findViewById(R.id.prev)
        next_but = findViewById(R.id.next)
        mouse?.isSelected = true
        current_page = findViewById<TextView>(R.id.page)
        start_cur()
        start_pen()
        start_high_light()
        start_erase()
        redo_but!!.setOnClickListener(View.OnClickListener { a: View? ->
            pageImage!!.my_redo()
        })
        undo_but!!.setOnClickListener(View.OnClickListener { a: View? ->
            pageImage!!.my_undo()
        })

        try {
            openRenderer(this)
        } catch (exception: IOException) {
            Log.d(LOGNAME, "Error opening PDF")
        }
        pdf = findViewById(R.id.pdf)
        pageImage = PDFimage(this)
        pageImage!!.make_PDFimage(pdfRenderer!!.pageCount)
        pageImage!!.minimumWidth = 1000
        pageImage!!.minimumHeight = 2000
        pdf!!.addView(pageImage)

        // start on page 0
        showPage(0)
        prev_but!!.setOnClickListener { v: View ->
            if (currentPage!!.index > 0) {
                showPage(currentPage!!.index - 1)
            }
        }

        next_but!!.setOnClickListener { v: View ->
            if (currentPage!!.index< pdfRenderer!!.pageCount - 1) {
                showPage(currentPage!!.index + 1)
            }
        }

    }


    fun showPage(index: Int) {
        if (currentPage != null) {
            currentPage!!.close();
        }
        currentPage = pdfRenderer!!.openPage(index)
        val bitmap = Bitmap.createBitmap(
            currentPage!!.getWidth(),
            currentPage!!.getHeight(),
            Bitmap.Config.ARGB_8888
        )
        currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        if (currentPage!!.getIndex() > 0) {
            current_page!!.setText((currentPage!!.getIndex() + 1).toString())
            prev_but!!.isEnabled = true
        }
        if (currentPage!!.getIndex() + 1 < pdfRenderer!!.pageCount) {
            current_page!!.setText((currentPage!!.getIndex() + 1).toString())
            next_but!!.isEnabled = true
        }
        pageImage!!.setImage(bitmap, index)
    }


    public override fun onSaveInstanceState(outState : Bundle ) {

        super.onSaveInstanceState(outState);

        outState.putString("CUSTOM_CLASS", pageImage.toString())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val json = savedInstanceState?.getString("CUSTOM_CLASS")
        if (!json!!.isEmpty()) {
            //pageImage = json
        }
    }

    @Throws(IOException::class)
    private fun openRenderer(context: Context) {
        // In this sample, we read a PDF from the assets directory.
        val file = File(context.cacheDir, FILENAME)
        if (!file.exists()) {
            // pdfRenderer cannot handle the resource directly,
            // so extract it into the local cache directory.
            val asset = this.resources.openRawResource(FILERESID)
            val output = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var size: Int
            while (asset.read(buffer).also { size = it } != -1) {
                output.write(buffer, 0, size)
            }
            asset.close()
            output.close()
        }
        parcelFileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        // capture PDF data
        // all this just to get a handle to the actual PDF representation
        pdfRenderer = PdfRenderer(parcelFileDescriptor)
    }

    // do this before you quit!

    @Throws(IOException::class)
    private fun closeRenderer() {
        currentPage?.close()
        pdfRenderer.close()
        parcelFileDescriptor.close()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            var lp= pdf!!.getLayoutParams();
            lp.height= 1300;
            pdf!!.setLayoutParams(lp);
            pageImage!!.init_scale()
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            var lp= pdf!!.getLayoutParams();
            lp.height= 2050;
            pdf!!.setLayoutParams(lp);
            // Gets the layout params that will allow you to resize the layout
            pageImage!!.init_scale()
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStop() {
        super.onStop()
        try {
            closeRenderer()
        } catch (ex: IOException) {
            Log.d(LOGNAME, "Unable to close PDF renderer")
        }
    }




    private fun show_Page(index: Int) {
        if (pdfRenderer.pageCount <= index) {
            return
        }
        // Close the current page before opening another one.
        currentPage?.close()

        // Use `openPage` to open a specific page in PDF.
        currentPage = pdfRenderer.openPage(index)

        if (currentPage != null) {
            // Important: the destination bitmap must be ARGB (not RGB).
            val bitmap = Bitmap.createBitmap(currentPage!!.getWidth(), currentPage!!.getHeight(), Bitmap.Config.ARGB_8888)

            // Here, we render the page onto the Bitmap.
            // To render a portion of the page, use the second and third parameter. Pass nulls to get the default result.
            // Pass either RENDER_MODE_FOR_DISPLAY or RENDER_MODE_FOR_PRINT for the last parameter.
            currentPage!!.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Display the page
            //pageImage!!.setImage(bitmap)
        }
    }
}