val rows by dao.observeAll().collectAsState(initial = emptyList())
val balance by dao.observeBalance().collectAsState(initial = 0)
val count = quantity.toIntOrNull()?.takeIf { it in 1..99 }
items(rows, key = { it.id }) { row ->
