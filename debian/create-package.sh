#!/bin/sh

# Constructs a Debian package containing the project's jar files.
# Authors: Romain Lenglet <romain.lenglet@rd.francetelecom.com>
#          Eric Bruneton <eric.bruneton@rd.francetelecom.com>

# This script takes six arguments:
# - the project base directory
# - the directory that contains the jar files to be included into the package
# - the root directory to be used to construct the package files
# - the project name
# - the project version
# - the directory into which the debian package file must be put

BASE_DIR="$1"
LIBS_DIR="$2"
DEST_DIR="$3"
PROJECT_NAME="$4"
PROJECT_VERSION="$5"
BUILD_DIR="$6"

rm -Rf "${DEST_DIR}"
mkdir "${DEST_DIR}"
mkdir "${DEST_DIR}/usr"
mkdir "${DEST_DIR}/usr/share"

echo "Preparing library files..."

mkdir "${DEST_DIR}/usr/share/java"
cp "${LIBS_DIR}"/*.jar "${DEST_DIR}/usr/share/java"
(
cd "${DEST_DIR}/usr/share/java"
for FILE in *.jar
do
   NAME=`echo ${FILE} | sed -e 's/\(.*\).jar/\1/'`
   mv "${NAME}.jar" "${NAME}-${PROJECT_VERSION}.jar"
   ln -s "${NAME}-${PROJECT_VERSION}.jar" "${NAME}.jar"
done
)

echo "Preparing documentation files..."

mkdir "${DEST_DIR}/usr/share/doc"
mkdir "${DEST_DIR}/usr/share/doc/${PROJECT_NAME}"
cp -r "${BASE_DIR}/debian/files/doc"/* "${DEST_DIR}/usr/share/doc/${PROJECT_NAME}"

echo "Preparing package control files..."

mkdir "${DEST_DIR}/DEBIAN"
cp "${BASE_DIR}/debian/files/control"/* "${DEST_DIR}/DEBIAN"

echo "Preparing file rights..."

chmod 755 `find "${DEST_DIR}" -type d`
chmod 644 `find "${DEST_DIR}" -type f`

echo "Creating debian package..."

dpkg-deb -b "${DEST_DIR}" "${BUILD_DIR}"

echo "Removing temporary files..."

rm -Rf "${DEST_DIR}"

echo "Debian package created"
