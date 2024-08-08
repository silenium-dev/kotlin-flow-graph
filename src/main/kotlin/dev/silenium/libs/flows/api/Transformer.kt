package dev.silenium.libs.flows.api

interface Transformer<IT, IP, OT, OP> : Sink<IT, IP>, Source<OT, OP>
