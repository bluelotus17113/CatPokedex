package com.catpokedex.data

enum class CatType(val displayName: String, val colorHex: Long) {
    NORMAL("Normal", 0xFFA8A29E),
    FUEGO("Fuego", 0xFFC4906A),
    AGUA("Agua", 0xFF7BA7BC),
    PLANTA("Planta", 0xFF8FA68E),
    ELECTRICO("Eléctrico", 0xFFC4A85A),
    PSIQUICO("Psíquico", 0xFFA695B8),
    HIELO("Hielo", 0xFF8CBDBF),
    SINIESTRO("Siniestro", 0xFF6B6360),
    HADA("Hada", 0xFFC49BA8),
    FANTASMA("Fantasma", 0xFF8E84A0),
    LUCHADOR("Luchador", 0xFFBC8A7B),
    DRAGÓN("Dragón", 0xFF7B8FBF);

    companion object {
        fun fromDisplayName(name: String): CatType {
            return entries.find { it.displayName == name } ?: NORMAL
        }

        val allDisplayNames: List<String>
            get() = entries.map { it.displayName }
    }
}
