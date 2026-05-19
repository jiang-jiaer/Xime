package com.kingzcheung.xime.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CandidatePage(
    candidates: List<String>,
    candidateComments: List<String> = emptyList(),
    associationCandidates: List<String> = emptyList(),
    inputText: String,
    onCandidateSelect: (Int) -> Unit,
    onAssociationSelect: ((Int) -> Unit)? = null,
    backgroundColor: Color,
    textColor: Color,
    hasNextPage: Boolean = false,
    hasPrevPage: Boolean = false,
    onPageDown: (() -> Unit)? = null,
    onPageUp: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(8.dp)
    ) {
        if (candidates.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = inputText,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (hasPrevPage && onPageUp != null) textColor.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .clickable(
                                enabled = hasPrevPage && onPageUp != null,
                                onClick = { onPageUp?.invoke() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "上一页",
                            tint = if (hasPrevPage) textColor else textColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (hasNextPage && onPageDown != null) textColor.copy(alpha = 0.1f)
                                else Color.Transparent
                            )
                            .clickable(
                                enabled = hasNextPage && onPageDown != null,
                                onClick = { onPageDown?.invoke() }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "下一页",
                            tint = if (hasNextPage) textColor else textColor.copy(alpha = 0.3f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(candidates) { index, candidate ->
                    CandidatePageItem(
                        text = candidate,
                        comment = candidateComments.getOrElse(index) { "" },
                        onClick = { onCandidateSelect(index) },
                        textColor = textColor
                    )
                }
            }
        }
        
        if (associationCandidates.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "联想词",
                color = textColor.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(associationCandidates) { index, candidate ->
                    CandidatePageItem(
                        text = candidate,
                        comment = "",
                        onClick = { onAssociationSelect?.invoke(index) },
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CandidatePageItem(
    text: String,
    comment: String = "",
    onClick: () -> Unit,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val displayComment = comment.replace("~", "")
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(textColor.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .fillMaxWidth()
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(vertical = 8.dp)
        )
        if (displayComment.isNotEmpty()) {
            Text(
                text = displayComment,
                color = textColor.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-3).dp, y = (-4).dp)
            )
        }
    }
}