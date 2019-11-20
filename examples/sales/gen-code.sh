#! /bin/sh

PRG=$(readlink -f $0)
PRG_DIR=$(dirname "${PRG}")
OUTPUT_DIR=$(dirname "${PRG_DIR}")

java -Doutput.dir=${OUTPUT_DIR} \
    -Ddisable.journal=true \
    -Dsymbol.naming=unix_c \
    -jar ~/github/apuex/lagom-codegen/codegen/target/scala-2.12/lagom-codegen-1.0.0.jar \
    generate-all \
    ${PRG_DIR}/model/src/test/resources/sales_entities.xml


