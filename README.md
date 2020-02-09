mmlTools
=======
[![Build Status](https://travis-ci.org/logue/mmlTools.svg?branch=master)](https://travis-ci.org/logue/mmlTools)

### 最新版のダウンロード

* リリースを参照ください https://github.com/logue/mmlTools/releases
* Windowsのみです.


### なにをしたいの？

* マビノギ内で演奏してもズレないMMLを作りたい。


### このプログラムは？

* mmlToolsは、マビノギMMLを扱うためのツール群です。
* MabiIccoは、mmlToolsを使用したマビノギMMLのエディタです。
* Java 8以降が必要です。
* Javaなので、JREとDLSファイルがあればWinさん以外でも起動はします。


### MabiIcco

* ピアノ世代対応。（オクターブ０も）
* 歌パート対応。
* テンポ指定がスコア単位なので、各パートにそれぞれテンポ指定は不要です。
* MMLの調整を行う機能をもっています。（演奏時間補正など）
* 作成した楽譜（MML文字列ではなく）をマビノギ演奏に最も近づける！（マビノギにもっていってはじめておかしい！と気づくようなことをなくしたい）

### ビルド方法

oracleのJava8のサポート終了に伴い、本派生版では[Liberica JDK8のFullバージョン](https://bell-sw.com/pages/java-8u242/)を用いてビルドしています。
jdk8がすでにインストールされている場合は、あらかじめアンインストールした後にインストールしてください。

※openjdk8や、AdoptOpenJDK8には、本プロジェクトで必要になるjavafxが含まれていないため、別途javaxをビルドしたりインストールする必要があります。

また、ビルドコマンド実行のため、Apache Antが必要です。

インストーラーを生成する必要がある場合は、[WiX Toolset](https://wixtoolset.org)を別途インストールし、`PATH`を通してください。

プロジェクトルートで、`ant`コマンドを実行するとソースがビルドされます。
`ant jar`でMabiIcco.jarが生成され、`ant deploy`でインストーラーが生成されます。

### 例

[![](https://img.youtube.com/vi/7RXuQOZL-xY/0.jpg)](https://www.youtube.com/watch?v=7RXuQOZL-xY)
