package com.roberto.clientesgestor.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun ResumenCard(


    titulo: String,
    cantidad : Int,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier

) {

    Card(


        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = androidx.compose.ui.graphics.Color.White
        )


    ){

        Column(

            horizontalAlignment = Alignment.CenterHorizontally

        ){
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = color




            ){}

            Text(
                text = titulo,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(4.dp)
            )



            Spacer(
                modifier = Modifier.height(8.dp)

            )


            Text(
                text = cantidad.toString(),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(8.dp)

            )
        }

    }

}