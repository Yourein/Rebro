package net.yourein.rebro.interfaces

import net.yourein.rebro.model.ndl.SruResponse

interface NdlRepository {
    suspend fun searchByIsbn(isbn: String): SruResponse?
}
