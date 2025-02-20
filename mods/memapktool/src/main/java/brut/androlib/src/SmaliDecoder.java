/**
 * Copyright (C) 2018 Ryszard Wiśniewski <brut.alll@gmail.com>
 * Copyright (C) 2018 Connor Tumbleson <connor.tumbleson@gmail.com>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package brut.androlib.src;

import brut.androlib.AndrolibException;
import org.jf.baksmali.Baksmali;
import org.jf.baksmali.BaksmaliOptions;
import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.InlineMethodResolver;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedOdexFile;

import java.io.File;
import java.io.IOException;

/**
 * @author Ryszard Wiśniewski <brut.alll@gmail.com>
 */
public class SmaliDecoder {

    private final File mApkFile;
    private final File mOutDir;
    private final String mDexFile;
    private final boolean mBakDeb;
    private final int mApi;

    private SmaliDecoder(File apkFile, File outDir, String dexName, boolean bakdeb, int api) {
        mApkFile = apkFile;
        mOutDir = outDir;
        mDexFile = dexName;
        mBakDeb = bakdeb;
        mApi = api;
    }

    public static void decode(File apkFile, File outDir, String dexName, boolean bakdeb, int api)
            throws AndrolibException {
        new SmaliDecoder(apkFile, outDir, dexName, bakdeb, api).decode();
    }

    private void decode() throws AndrolibException {
        try {
            final BaksmaliOptions options = new BaksmaliOptions();

            // options
            options.deodex = false;
            options.implicitReferences = false;
            options.parameterRegisters = true;
            options.localsDirective = true;
            options.sequentialLabels = true;
            options.debugInfo = mBakDeb;
            options.codeOffsets = false;
            options.accessorComments = false;
            options.registerInfo = 0;
            options.inlineResolver = null;

            // set jobs automatically
            int jobs = Runtime.getRuntime().availableProcessors();
            if (jobs > 6) {
                jobs = 6;
            }

            // create the dex
            DexBackedDexFile dexFile = DexFileFactory.loadDexEntry(mApkFile, mDexFile, true, Opcodes.forApi(mApi));

            if (dexFile.isOdexFile()) {
                throw new AndrolibException("Warning: You are disassembling an odex file without deodexing it.");
            }

            if (dexFile instanceof DexBackedOdexFile) {
                options.inlineResolver =
                        InlineMethodResolver.createInlineMethodResolver(((DexBackedOdexFile) dexFile).getOdexVersion());
            }

            Baksmali.disassembleDexFile(dexFile, mOutDir, jobs, options);
        } catch (IOException ex) {
            throw new AndrolibException(ex);
        }
    }
}
