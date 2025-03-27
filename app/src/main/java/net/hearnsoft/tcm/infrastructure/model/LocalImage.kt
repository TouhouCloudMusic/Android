package net.hearnsoft.tcm.infrastructure.model

import net.hearnsoft.tcm.domain.model.banner.BannerImage

data class LocalImage(
    override val id: Int,
    override val contentUrl: String?,
    override val altText: String = "TODO",
) : BannerImage {
    override val resourceUrl = null;
}
