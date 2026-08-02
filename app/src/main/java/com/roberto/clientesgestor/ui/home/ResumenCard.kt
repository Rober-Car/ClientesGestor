package com.roberto.clientesgestor.ui.home

import android.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp


@Composable
fun ResumenCard(


    titulo: String,
    cantidad : Int,
    color: Color,
    onClick: () -> Unit

) {

    Card(



        onClick = onClick,
        modifier = Modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )


    ){

        Column(){}

    }

}