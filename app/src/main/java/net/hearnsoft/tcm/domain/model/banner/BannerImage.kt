package net.hearnsoft.tcm.domain.model.banner

interface BannerImage {
    /**
     * The id of local banner image
     */
    val id: Int?

    /**
     * The URL of remote banner image
     */
    val resourceUrl: String?

    /**
     * The URL to redirect to when the banner is pressed
     */
    val contentUrl: String?

    /**
     * Alt text of the image
     */
    val altText: String
}