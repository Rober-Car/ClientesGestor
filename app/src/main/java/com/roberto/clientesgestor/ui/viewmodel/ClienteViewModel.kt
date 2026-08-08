package com.roberto.clientesgestor.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.roberto.clientesgestor.data.repository.ClienteRepository

class ClienteViewModel(
    private val clienteRepository: ClienteRepository
) : ViewModel() {

}