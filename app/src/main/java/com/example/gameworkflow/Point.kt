package com.example.gameworkflow

data class Point(val x: Int, val y: Int)

private fun Point.constrainTo(bounds: Point): Point = copy(
    x = x.coerceIn(0, bounds.x - 1),
    y = y.coerceIn(0, bounds.y - 1)
)

private fun Point.moved(direction: Direction): Point = copy(
    x = x + direction.dx,
    y = y + direction.dy
)

fun Point.moved(
    direction: Direction,
    bounds: Point
): Point = moved(direction).constrainTo(bounds)
