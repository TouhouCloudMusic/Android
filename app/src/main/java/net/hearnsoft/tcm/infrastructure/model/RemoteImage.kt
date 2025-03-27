package net.hearnsoft.tcm.infrastructure.model

import net.hearnsoft.tcm.domain.model.banner.BannerImage

data class RemoteImage(
    override val resourceUrl: String,
    override val contentUrl: String?,
    override val altText: String = "TODO",
) : BannerImage {
    override val id = null;
}
