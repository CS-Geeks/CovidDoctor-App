package az.omar.coviddoctor.ui

import android.app.ProgressDialog
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import az.omar.coviddoctor.R
import az.omar.coviddoctor.adapter.NewsAdapter
import az.omar.coviddoctor.ml.CovidModelLite
import az.omar.coviddoctor.pojo.Article
import az.omar.coviddoctor.utils.Utils.mTempFileExtra
import kotlinx.android.synthetic.main.activity_result.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer

class ResultActivity : AppCompatActivity() {

    lateinit var newsAdapter: NewsAdapter
    private var dialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val tempFile = intent.getStringExtra(mTempFileExtra)
        Log.e("TAG", "onCreate: $tempFile")
        val bitmap: Bitmap = BitmapFactory.decodeStream(openFileInput(tempFile))
        dialog = ProgressDialog.show(
            this, "",
            "Please wait while examining your photo..."
        )

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val results = ruAiModel(bitmap)
                withContext(Dispatchers.Main) {
                    dialog?.dismiss()
                    checkResultsFromAIModel(results)
                }
            }

        }
        setUpRecyclerView()

    }

    private fun setUpRecyclerView() {
        newsAdapter = NewsAdapter()
        rv_learn_more.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            setHasFixedSize(true)
        }

        getNewsData()
    }

    private fun getNewsData() {
        newsAdapter.submitList(
            listOf(
                Article("name", R.drawable.run_check),
                Article("name", R.drawable.have_corona),
                Article("name", R.drawable.run_check2),
                Article("name", R.drawable.have_corona)
            )
        )
    }

    private fun ruAiModel(bitmap: Bitmap): DoubleArray {
        val image = Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        val imageSize: Int = image.rowBytes * image.height * 3
        val uncompressedBuffer = ByteBuffer.allocateDirect(imageSize)
        uncompressedBuffer.rewind()
        image.copyPixelsToBuffer(uncompressedBuffer)

        val inputFeature0 = TensorBuffer.createFixedSize(
            intArrayOf(1, image.width, image.height, 3),
            DataType.FLOAT32
        )

        inputFeature0.loadBuffer(uncompressedBuffer)

        val model = CovidModelLite.newInstance(this)
        val outputs = model.process(inputFeature0)
        val props = outputs.outputFeature0AsTensorBuffer.floatArray

        Log.e(
            "TAG", "ruAiModel: ${props[0].toDouble()}\n" +
                    "${props[1].toDouble()}"
        )
        model.close()
        return doubleArrayOf(props[0].toDouble(), props[1].toDouble())
    }

    private fun checkResultsFromAIModel(results: DoubleArray) {
        if (results[0] > results[1]) {
            iv_test_result.setImageResource(R.drawable.have_corona)
            tv_text_result.text = getString(R.string.have_corona)
        } else {
            iv_test_result.setImageResource(R.drawable.corona_free)
            tv_text_result.text = getString(R.string.do_not_have_corona)
        }
    }
}