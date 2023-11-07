package com.herick.viacep

import android.R
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isVisible
import com.herick.viacep.api.Api
import com.herick.viacep.databinding.ActivityMainBinding
import com.herick.viacep.model.Endereco
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var cep :String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        window.statusBarColor = Color.parseColor("#48B0E1")
        val action = supportActionBar
        action?.setBackgroundDrawable((ColorDrawable(Color.parseColor("#48B0E1"))))

        //configurando retrofit
        val retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl("https://viacep.com.br/ws/")
            .build()
            .create(Api::class.java)

        binding.btnBuscarCep.setOnClickListener {
            cep = binding.editCep.text.toString()
            binding.progressBar.isVisible = true

            if (cep.isEmpty()) {
                esconderTeclado()
                Toast.makeText(this, "Preencha o Cep", Toast.LENGTH_SHORT).show()
                binding.progressBar.isVisible = true
            } else {
                esconderTeclado()
                retrofit.setEndereco(cep).enqueue(object : Callback<Endereco> {
                    override fun onResponse(call: Call<Endereco>, response: Response<Endereco>) {
                        if (response.code() == 200) {
                            val logradouro = response.body()?.logradouro.toString()
                            val bairro = response.body()?.bairro.toString()
                            val localidade = response.body()?.localidade.toString()
                            val uf = response.body()?.uf.toString()
                            setFormularios(logradouro, bairro, localidade, uf)
                            binding.btnVerMapa.isVisible = true

                        }else{
                            binding.progressBar.isVisible = false
                            Toast.makeText(applicationContext, "CEP inválido!", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<Endereco>, t: Throwable) {
                        binding.progressBar.isVisible = false
                        Toast.makeText(applicationContext, "Erro inesperado..", Toast.LENGTH_SHORT).show()
                    }

                })
            }
        }

        binding.btnVerMapa.setOnClickListener {
            val cepVerMapa = cep // Substitua "SEU_CEP_AQUI" pelo CEP desejado

            val gmmIntentUri = Uri.parse("geo:0,0?q=$cepVerMapa") // Cria a URI com o CEP
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            mapIntent.setPackage("com.google.android.apps.maps") // Define o pacote para o Google Maps

            if (mapIntent.resolveActivity(packageManager) != null) {
                startActivity(mapIntent) // Abre o Google Maps
            }
        }


    }


    private fun setFormularios(logradouro: String, bairro: String, localidade: String, uf: String) {
        binding.editLongradouro.setText(logradouro)
        binding.editBairro.setText(bairro)
        binding.editCidade.setText(localidade)
        binding.estado.setText(uf)
        binding.progressBar.isVisible = false
    }

    fun esconderTeclado(){
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // Esconde o teclado
        inputMethodManager.hideSoftInputFromWindow(binding.btnBuscarCep.windowToken, 0)
    }

}