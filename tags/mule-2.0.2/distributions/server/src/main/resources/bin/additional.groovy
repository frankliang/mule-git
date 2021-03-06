/*
 * $Id: additional.groovy 298 2008-01-31 00:42:47Z andrew $
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

File wrapperConfigFile = new File(args[0])
jpdaOpts = args[1]

// extracting wrapper conf directory
m = wrapperConfigFile.path =~ /^.*[\\\/]/
m.matches()
String wrapperConfDir = m[0]

File wrapperAdditionalConfFile = new File(wrapperConfDir + 'wrapper-additional.conf')
boolean debugEnabled = args.findIndexOf { '-debug'.equalsIgnoreCase(it)} > -1
int profileArgIndex = args.findIndexOf { '-profile'.equalsIgnoreCase(it)}
boolean profileEnabled = profileArgIndex > -1
boolean adHocOptionsAvailable = args.findIndexOf { it.startsWith('-M') } > -1

paramIndex = 0

if (debugEnabled || profileEnabled || adHocOptionsAvailable) {
    // looking for maximum number of wrapper.java.additional property
    wrapperConfigFile.eachLine {
        String line ->
        switch (line) {
            case ~/^\s*wrapper\.java\.additional\..+/:
                m = line =~ /^\s*wrapper\.java\.additional\.(\d+).+/
                m.find()
                paramIndex = Math.max(Integer.valueOf(m[0][1]), paramIndex)
                break
        }
    }
    paramIndex++

    wrapperAdditionalConfFile.withWriter() {
        Writer w ->
        w << "# Do not edit this file!\n"
        w << "# This is a generated file to add additional parameters to JVM\n"

        if (debugEnabled) {
            writeJpdaOpts(w)
        }

        if (profileEnabled) {
            String profileArg
            if (args.size() - 1 > profileArgIndex ) {
                if (args[profileArgIndex + 1][0] != '-') {
                    profileArg = args[profileArgIndex + 1]
                }
            }
            writeProfilerOpts(w, profileArg)
        }

        if (adHocOptionsAvailable) {
            writeAdHocProps(w)
        }
    }

} else {
    // remove file if none of the options are specified
    if (wrapperAdditionalConfFile.exists()) {
        wrapperAdditionalConfFile.delete()
    }
}


//=== next goes procedures definitions

/**
    Ad-hoc options
*/
def void writeAdHocProps(Writer w) {
    args.findAll { it.startsWith('-M') }.each { arg ->
        w << "wrapper.java.additional.${paramIndex}=\"${arg.replaceFirst("^-M", "")}\"\n"
        w << "wrapper.java.additional.${paramIndex}.stripquotes=TRUE\n"
        paramIndex++
    }
}

def void writeJpdaOpts(Writer w) {
    def jvmArgs = []
    jpdaOpts.split("\\s-").each {jvmArgs << it}

    jvmArgs.each {String arg ->
        w << "wrapper.java.additional.${paramIndex++}=-${arg.replaceFirst("^-", "")}\n"
    }
}

def void writeProfilerOpts(Writer w, String optionValue) {
    if (optionValue == null || optionValue.length() == 0) {
        optionValue = "sessionname=Mule"
    }

    Double javaVersion = new Double(System.getProperty("java.specification.version"))
    String additional
    if (javaVersion >= 1.5) {
        additional = "-agentlib:${getLibraryName()}=$optionValue"
    } else {
        additional = "-Xrun${getLibraryName()}:$optionValue"
    }
    w << "wrapper.java.additional.${paramIndex++}=$additional\n"
}

def String getLibraryName() {
    // Decide whether this is a 32 or 64 bit version of Java.
    String jvmBits = System.getProperty("sun.arch.data.model", "")

    // Generate an os name.  Most names are used as is, but some are modified.
    String os = System.getProperty("os.name", "").toLowerCase()

    switch (os) {
        case ~/^windows.*/:
            return "yjpagent-win-$jvmBits"
            break
        case "sunos":
            return "yjpagent-solaris-${getArchitecture()}-$jvmBits"
            break
        case ~/^mac.*/:
            return "yjpagent-mac"
            break
        default: // should be some *nix
            return "yjpagent-linux-$jvmBits"
    }
}

def String getArchitecture() {
    // Generate architecture name.
    String arch = System.getProperty("os.arch", "").toLowerCase()

    switch (arch) {
        case ["amd64", "athlon", "ia32", "ia64", "x86_64", "i686", "i586", "i486", "i386", "x86"]:
            return "x86"
            break
        case ~/^sparc.*/:
            return "sparc"
            break
        default:
            println "ERROR: Architecture $arch is not supported by profiler"
            return
    }
}
