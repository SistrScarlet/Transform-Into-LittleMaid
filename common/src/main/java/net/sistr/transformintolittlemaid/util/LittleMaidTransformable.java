package net.sistr.transformintolittlemaid.util;

public interface LittleMaidTransformable {

    boolean isTransformedLittleMaid_TLM();

    void setTransformedLittleMaid_TLM(boolean transformed);

    /**
     * 変身中にメイドさんモデルのサイズ(当たり判定・視点高さ)を適用するか。
     *
     * <p>クライアント側コンフィグの値をサーバーへ同期したもの。当たり判定に関わるため、片側だけで判断すると クライアントとサーバーで食い違う。
     */
    boolean isApplyMaidSize_TLM();

    void setApplyMaidSize_TLM(boolean applyMaidSize);
}
