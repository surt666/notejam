(defproject notejam-common-layer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.amazonaws/aws-lambda-java-core "1.2.0"]
                 [cheshire "5.6.3"]
                 [amazonica "0.3.146" :exclusions [com.amazonaws/aws-java-sdk
                                                ;   com.amazonaws/amazon-kinesis-client
                                                   ]]
                 [com.amazonaws/aws-java-sdk-core "1.11.637"]
                 [com.amazonaws/aws-java-sdk-dynamodb "1.11.637"]
                 [com.amazonaws/aws-java-sdk-s3 "1.11.637"]]
  :repl-options {:init-ns notejam-common-layer.core})
