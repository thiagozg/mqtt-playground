package br.com.thiagozg.mqtt.main

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import br.com.thiagozg.mqtt.R
import kotlinx.android.synthetic.main.activity_receive.*

class ReceiveActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)
        bindViews()
    }

    private fun bindViews() {
        tvJson.text = intent.getStringExtra(KEY_JSON)?.formatToJsonStyle()
            ?: getString(R.string.empty_json)
    }

    private fun String.formatToJsonStyle(): String {
        val json = StringBuilder()
        var indentString = ""

        for (i in 0 until length) {
            val letter = this[i]
            when (letter) {
                '{', '[' -> {
                    json.append("\n$indentString$letter\n")
                    indentString += "\t"
                    json.append(indentString)
                }

                '}', ']' -> {
                    indentString = indentString.replaceFirst("\t".toRegex(), "")
                    json.append("\n$indentString$letter")
                }

                ',' -> json.append("$letter\n$indentString")

                else -> json.append(letter)
            }
        }

        return json.toString()
    }

    companion object {
        const val KEY_JSON = "keyJson"
    }

}