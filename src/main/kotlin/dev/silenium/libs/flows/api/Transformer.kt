package dev.silenium.libs.flows.api

/**
 * A [Transformer] is a flow element that transforms flow items.
 * It is simply both a [Sink] and a [Source].
 *
 * @param IT The type of the input data.
 * @param IP The type of the input metadata.
 * @param OT The type of the output data.
 * @param OP The type of the output metadata.
 * @see Sink
 * @see Source
 */
interface Transformer<IT, IP, OT, OP> : Sink<IT, IP>, Source<OT, OP>
