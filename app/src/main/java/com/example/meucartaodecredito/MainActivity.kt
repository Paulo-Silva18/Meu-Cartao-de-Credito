package com.example.meucartaodecredito

import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.ViewFlipper
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView

class MainActivity : AppCompatActivity() {

    // ---- Views ----
    private lateinit var cardFlipper: ViewFlipper
    private lateinit var cardFront: CardView
    private lateinit var cardBack: CardView

    private lateinit var tvBrandLogo: TextView
    private lateinit var tvCardNumberDisplay: TextView
    private lateinit var tvHolderDisplay: TextView
    private lateinit var tvValidityDisplay: TextView
    private lateinit var tvCvvDisplay: TextView

    private lateinit var etCardNumber: EditText
    private lateinit var etHolder: EditText
    private lateinit var etValidity: EditText
    private lateinit var etCvv: EditText

    private lateinit var tvErrorMessage: TextView
    private lateinit var btnConfirmar: Button

    // Controla se o cartão já está mostrando o verso, pra não animar duas vezes
    private var showingBack = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        setupTextWatchers()
        setupFocusListeners()
        setupConfirmButton()
    }

    private fun bindViews() {
        cardFlipper = findViewById(R.id.cardFlipper)
        cardFront = findViewById(R.id.cardFront)
        cardBack = findViewById(R.id.cardBack)

        tvBrandLogo = findViewById(R.id.tvBrandLogo)
        tvCardNumberDisplay = findViewById(R.id.tvCardNumberDisplay)
        tvHolderDisplay = findViewById(R.id.tvHolderDisplay)
        tvValidityDisplay = findViewById(R.id.tvValidityDisplay)
        tvCvvDisplay = findViewById(R.id.tvCvvDisplay)

        etCardNumber = findViewById(R.id.etCardNumber)
        etHolder = findViewById(R.id.etHolder)
        etValidity = findViewById(R.id.etValidity)
        etCvv = findViewById(R.id.etCvv)

        tvErrorMessage = findViewById(R.id.tvErrorMessage)
        btnConfirmar = findViewById(R.id.btnConfirmar)
    }

    // =====================================================================
    // TEXT WATCHERS — máscaras + atualização do card em tempo real
    // =====================================================================

    private fun setupTextWatchers() {

        // ---------- NÚMERO DO CARTÃO ----------
        etCardNumber.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false
            private var previousLength = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousLength = s?.length ?: 0
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s == null) return
                isUpdating = true

                val digitsOnly = s.toString().filter { it.isDigit() }.take(16)
                val masked = digitsOnly.chunked(4).joinToString(" ")

                if (masked != s.toString()) {
                    etCardNumber.setText(masked)
                    // mantém o cursor no fim (comportamento simples e previsível)
                    etCardNumber.setSelection(masked.length)
                }

                // Atualiza o cartão visual
                tvCardNumberDisplay.text =
                    if (digitsOnly.isEmpty()) "•••• •••• •••• ••••" else buildDisplayNumber(digitsOnly)

                // Detecta a bandeira a cada mudança
                detectBrand(digitsOnly)

                isUpdating = false
            }
        })

        // ---------- NOME DO TITULAR ----------
        etHolder.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString().orEmpty()
                tvHolderDisplay.text = if (text.isBlank()) "NOME DO TITULAR" else text.uppercase()
            }
        })

        // ---------- VALIDADE (MM/AA) ----------
        etValidity.addTextChangedListener(object : TextWatcher {
            private var isUpdating = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating || s == null) return
                isUpdating = true

                val digits = s.toString().filter { it.isDigit() }.take(4)
                val masked = when {
                    digits.length <= 2 -> digits
                    else -> "${digits.substring(0, 2)}/${digits.substring(2)}"
                }

                if (masked != s.toString()) {
                    etValidity.setText(masked)
                    etValidity.setSelection(masked.length)
                }

                tvValidityDisplay.text = masked.ifBlank { "MM/AA" }
                isUpdating = false
            }
        })

        // ---------- CVV ----------
        etCvv.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val digits = s?.toString().orEmpty()
                tvCvvDisplay.text = if (digits.isBlank()) "•••" else "•".repeat(digits.length)
            }
        })
    }

    /** Reconstrói "1234 5678 9012 3456" a partir só dos dígitos. */
    private fun buildDisplayNumber(digits: String): String {
        val padded = digits.padEnd(16, '•')
        return padded.chunked(4).joinToString(" ")
    }

    // =====================================================================
    // DESAFIO 2 — Detecção dinâmica da bandeira
    // =====================================================================

    private fun detectBrand(digits: String) {
        when {
            digits.startsWith("4") -> {
                tvBrandLogo.text = "VISA"
                cardFront.setCardBackgroundColor(0xFF1A1F71.toInt())
                cardBack.setCardBackgroundColor(0xFF1A1F71.toInt())
            }
            isMastercardPrefix(digits) -> {
                tvBrandLogo.text = "MASTERCARD"
                cardFront.setCardBackgroundColor(0xFF101A2C.toInt())
                cardBack.setCardBackgroundColor(0xFF101A2C.toInt())
            }
            digits.isEmpty() -> {
                tvBrandLogo.text = "CARTÃO"
                cardFront.setCardBackgroundColor(0xFF101A2C.toInt())
                cardBack.setCardBackgroundColor(0xFF101A2C.toInt())
            }
            else -> {
                tvBrandLogo.text = "OUTRA"
                cardFront.setCardBackgroundColor(0xFF37474F.toInt())
                cardBack.setCardBackgroundColor(0xFF37474F.toInt())
            }
        }
    }

    /** Mastercard: faixa 51–55 ou 2221–2720 (padrão oficial da bandeira). */
    private fun isMastercardPrefix(digits: String): Boolean {
        if (digits.length < 2) return false
        val twoDigits = digits.substring(0, 2).toIntOrNull() ?: return false
        if (twoDigits in 51..55) return true

        if (digits.length < 4) return false
        val fourDigits = digits.substring(0, 4).toIntOrNull() ?: return false
        return fourDigits in 2221..2720
    }

    // =====================================================================
    // DESAFIO 1 — Flip do cartão ao focar no CVV
    // =====================================================================

    private fun setupFocusListeners() {
        etCvv.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) flipToBack() else flipToFront()
        }

        val goToFrontListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) flipToFront()
        }
        etCardNumber.onFocusChangeListener = goToFrontListener
        etHolder.onFocusChangeListener = goToFrontListener
        etValidity.onFocusChangeListener = goToFrontListener
    }

    private fun flipToBack() {
        if (showingBack) return
        showingBack = true
        animateFlip(cardFlipper.getChildAt(0), cardFlipper.getChildAt(1))
        cardFlipper.showNext()
    }

    private fun flipToFront() {
        if (!showingBack) return
        showingBack = false
        animateFlip(cardFlipper.getChildAt(1), cardFlipper.getChildAt(0))
        cardFlipper.showPrevious()
    }

    /** Gira a view atual 90° no eixo Y, troca o conteúdo e gira a próxima de volta a 0°. */
    private fun animateFlip(current: View, next: View) {
        val cameraDistance = 8000 * resources.displayMetrics.density
        current.cameraDistance = cameraDistance
        next.cameraDistance = cameraDistance

        val outAnim = ObjectAnimator.ofFloat(current, "rotationY", 0f, 90f)
        outAnim.duration = 150

        val inAnim = ObjectAnimator.ofFloat(next, "rotationY", -90f, 0f)
        inAnim.duration = 150
        inAnim.startDelay = 150

        AnimatorSet().apply {
            playTogether(outAnim, inAnim)
            start()
        }
    }

    // =====================================================================
    // VALIDAÇÃO
    // =====================================================================

    private fun setupConfirmButton() {
        btnConfirmar.setOnClickListener { validateAndConfirm() }
    }

    private fun validateAndConfirm() {
        val digits = etCardNumber.text.toString().filter { it.isDigit() }
        val name = etHolder.text.toString().trim()

        val error = when {
            digits.length != 16 -> "O número do cartão deve ter 16 dígitos."
            name.length < 3 -> "O nome do titular deve ter ao menos 3 caracteres."
            !isValidExpiry(etValidity.text.toString()) -> "Validade inválida. Use o formato MM/AA."
            etCvv.text.toString().length < 3 -> "CVV inválido."
            else -> null
        }

        if (error != null) {
            tvErrorMessage.text = error
            tvErrorMessage.visibility = View.VISIBLE
        } else {
            tvErrorMessage.visibility = View.GONE
            // Dados válidos — aqui entraria o processamento real (salvar, enviar, etc.)
        }
    }

    private fun isValidExpiry(masked: String): Boolean {
        val regex = Regex("""^(0[1-9]|1[0-2])/\d{2}$""")
        return regex.matches(masked)
    }
}