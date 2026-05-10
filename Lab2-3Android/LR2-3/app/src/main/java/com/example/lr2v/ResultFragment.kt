package com.example.lr2v

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class ResultFragment : Fragment() {

    private var listener: OnCancelListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnCancelListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnCancelListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        val tvResult = view.findViewById<TextView>(R.id.tvResult)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)

        val resultText = arguments?.getString(ARG_RESULT_TEXT) ?: "Немає даних для відображення"
        tvResult.text = resultText

        btnCancel.setOnClickListener {
            listener?.onCancelPressed()
        }

        return view
    }

    interface OnCancelListener {
        fun onCancelPressed()
    }

    companion object {
        private const val ARG_RESULT_TEXT = "result_text"

        fun newInstance(resultText: String): ResultFragment {
            val fragment = ResultFragment()
            val args = Bundle()
            args.putString(ARG_RESULT_TEXT, resultText)
            fragment.arguments = args
            return fragment
        }
    }
}