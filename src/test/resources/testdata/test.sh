#!/bin/sh
##
## test_dwebp.sh
##
## Author: John Koleszar <jkoleszar@google.com>
##
## Simple test driver for validating (via md5 sum) the output of the libwebp
## dwebp example utility.
##
## This file distributed under the same terms as libwebp. See the libwebp
## COPYING file for more information.
##

self=$0

usage() {
    echo "Usage: $self [--exec=/path/to/dwebp] /path/to/libwebp_tests.md5"
    exit 1
}

for opt; do
    optval=${opt##*=}
    case ${opt} in
        --exec=*) executable="${optval}";;
        -*) usage;;
        *) [ -z "$tests" ] || usage; tests="$opt";;
    esac
done

# Validate test file
if [ -z "$tests" ]; then
    [ -f "$(dirname $self)/libwebp_tests.md5" ] && tests="$(dirname $self)/libwebp_tests.md5"
fi
[ -f "$tests" ] || usage

# Validate test executable
executable=${executable:-java}
#"$executable" 2>/dev/null | grep -q Usage || usage

test_dir=$(dirname ${tests})
for f in $(awk '{print $2}' "$tests" | sed -e 's,webp\....,webp,' | uniq); do
    f="${test_dir}/${f}"

    # Decode the file to PPM and YUV
    #"${executable}" -o "${f}.ppm" "$f" >/dev/null
    "${executable}" -jar ../dwebp.jar "${f}.pgm" "$f" 2>&1|/dev/null

    # Check the md5sums
    grep ${f##*/} "$tests" | (cd $(dirname $f); md5sum -c -)

    # Clean up.
    rm -f ${f}.{ppm,pgm}
done
