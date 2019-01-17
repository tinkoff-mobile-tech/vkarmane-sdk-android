package me.vkarmane.sdksample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_result.*
import ru.tinkoff.vkarmane.sdk.VKarmaneSDK
import ru.tinkoff.vkarmane.sdk.VkarmaneSdkException

/**
 *  @author Sergei Solodkov
 */
class ResultActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)
        intent.data?.let { uri ->
            resultTextView.text = getData(uri)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.result_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_again) {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }

    private fun getData(uri: Uri): String {
        if (uri.authority == CANCEL_DEEPLINK) {
            return "Canceled"
        }
        val privateKey = KeysManager(this).getPrivateKey()
        return try {
            val result = VKarmaneSDK.getJsonFromLink(uri, privateKey)
            val resultJson = JsonParser().parse(result)
            prepareResult(resultJson)
        } catch (e: VkarmaneSdkException) {
            "Error: ${e.vKarmaneSDKError.name}"
        }
    }

    private fun prepareResult(json: JsonElement): String {
        val array = json.asJsonArray
        val gson = GsonBuilder().setPrettyPrinting().create()

        val stringBuilder = StringBuilder()
        for (i in 0 until array.size()) {
            stringBuilder.append(gson.toJson(array.get(i)).toString()).append("\n\n")
        }
        return stringBuilder.toString()
    }


}