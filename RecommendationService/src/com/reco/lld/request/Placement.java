package com.reco.lld.request;

/**
 * Surface that requested the slate — drives strategy selection.
 * <p>
 * Why: homepage, product-detail, and cart have different objectives
 * (exploration vs similar items vs complement).
 */
public enum Placement {
    HOME,
    PRODUCT_DETAIL,
    CART,
    EMAIL
}
