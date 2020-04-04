rm -rf test-run && \
  mkdir test-run && \
  cp -r src spec deps.edn test-run/ && \
  (cd test-run ; \
   mkdir classes; \
   clj -Rtest -e "(dorun (map compile
                              '(speclj.platform.SpecFailure
                                speclj.platform.SpecPending)))" && \
   clj -A:test)