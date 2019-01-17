package me.vkarmane.sdksample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.EditText
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import ru.tinkoff.vkarmane.sdk.DocumentKind
import ru.tinkoff.vkarmane.sdk.VKarmaneSDK

class MainActivity : AppCompatActivity() {

    private lateinit var docsAdapter:Adapter
    private lateinit var deeplinkInput: EditText

    private lateinit var publicKey: String
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        publicKey = KeysManager(this).getPublicKey()
        deeplinkInput = findViewById(R.id.mainDeeplinkInput)

        appInstalledTest()
        docsAdapter = Adapter { clickedItem ->
            docsAdapter.data.forEach {
                if (it.text == clickedItem.text) {
                    it.isChecked = !it.isChecked
                }
            }
            docsAdapter.notifyDataSetChanged()
            invalidateLink()
        }

        docsAdapter.data = initDocsList()

        with(mainDocList) {
            isNestedScrollingEnabled = false
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = docsAdapter
            addItemDecoration(DividerDecorator(this@MainActivity))
        }

        mainSubmitBtn.setOnClickListener {
            submit()
        }
        multiChoice.setOnCheckedChangeListener { _, _ ->
            invalidateLink()
        }
    }

    private fun invalidateLink() {
        val selectedDocs = docsAdapter.data.filter { it.isChecked }.map { DocumentKind.valueOf(it.text) }
        if (selectedDocs.isEmpty()) {
            mainDeeplinkInput.setText("")
        } else {
            mainDeeplinkInput.setText(buildLink(selectedDocs))
        }
    }

    private fun submit(){
        val link = mainDeeplinkInput.text.toString()
        if (link.isNotBlank()){
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(link)
            if (i.resolveActivity(packageManager) != null) {
                startActivity(i)
            } else {
                showToast(R.string.deeplink_error)
            }
        }
    }

    private fun showToast(@StringRes msg: Int) {
        toast?.cancel()
        toast = Toast.makeText(this, msg, Toast.LENGTH_LONG).also {
            it.show()
        }
    }

    private fun initDocsList() = DocumentKind.values().map { Item(it.name, false) }

    private fun buildLink(docs: List<DocumentKind>): String {
        return VKarmaneSDK.getDocumentsLinkBuilder()
            .setMultiChoice(multiChoice.isChecked)
            .setKinds(docs)
            .setXSource("Vkarmane Test")
            .setXCancelLink("$SDK_SCHEME://$CANCEL_DEEPLINK")
            .setXErrorLink("$SDK_SCHEME://$ERROR_DEEPLINK")
            .setXSuccessLink("$SDK_SCHEME://$SUCCESS_DEEPLINK")
            .setPublicKey(publicKey)
            .build()
    }

    private fun appInstalledTest(){
        val isInstalled = VKarmaneSDK.isAppInstalled(this)
        Toast.makeText(this, "App installed: $isInstalled", Toast.LENGTH_SHORT).show()
    }

}

const val SDK_SCHEME = "vkarmanesdkexample"
const val SUCCESS_DEEPLINK = "vkarmane-sdk-success"
const val ERROR_DEEPLINK = "vkarmane-sdk-error"
const val CANCEL_DEEPLINK = "vkarmane-sdk-cancel"
