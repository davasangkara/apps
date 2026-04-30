// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Jika menggunakan Version Catalog (libs.versions.toml), update versinya di file tersebut
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false

    // ATAU jika ingin deklarasi manual (pilih salah satu cara saja), gunakan format ini:
    // id 'com.android.application' version '8.5.0' apply false
}