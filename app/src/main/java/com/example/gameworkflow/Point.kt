package com.example.gameworkflow

data class Point(val x: Int, val y: Int)

fun Point.moved(direction: Direction): Point = copy(
    x = x + direction.dx,
    y = y + direction.dy
)

fun Point.constrainTo(bounds: Point): Point = copy(
    x = x.coerceIn(0, bounds.x - 1),
    y = y.coerceIn(0, bounds.y - 1)
)
