fun nameToId(name: String): String {
    return name.replace("[^A-Za-z0-9]".toRegex(), "-").lowercase()
}
