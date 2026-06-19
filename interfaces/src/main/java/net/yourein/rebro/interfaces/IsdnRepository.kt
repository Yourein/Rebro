package net.yourein.rebro.interfaces

import net.yourein.rebro.model.isdn.IsdnResponse

interface IsdnRepository {
    suspend fun getBookInfo(isdn: String): IsdnResponse?
}
