#!/usr/bin/env bash
#
# Generate a certificate hierarchy and export a .p12 file using OpenSSL.
# Based on doc/CERTIFICATE_GENERATE.md
#
# Usage:
#   ./generate-certificate.sh [-o OUTPUT_DIR] [-n NAME] [-p P12_PASSWORD] [-d DAYS] [-h]
#
#   -o  Output directory (default: ./certs)
#   -n  File name prefix (default: leaf)
#   -p  Password for the exported .p12 file (default: empty)
#   -d  Validity in days for every generated certificate (default: 3650)
#   -h  Show this help
#
set -euo pipefail

OUTPUT_DIR="./certs"
NAME="leaf"
P12_PASSWORD=""
DAYS=3650

# Distinguished names (adjust as needed)
ROOT_CA_SUBJECT="/C=EC/ST=Pichincha/L=Quito/O=RoQuiSigner/CN=RoQuiSigner Root CA"
INTERMEDIATE_CA_SUBJECT="/C=EC/ST=Pichincha/L=Quito/O=RoQuiSigner/CN=RoQuiSigner Intermediate CA"
LEAF_SUBJECT="/C=EC/ST=Pichincha/L=Quito/O=RoQuiSigner/CN=www.example.com"

usage() {
	cat <<EOF
Usage: $(basename "$0") [-o OUTPUT_DIR] [-n NAME] [-p P12_PASSWORD] [-d DAYS] [-h]

  -o  Output directory (default: ${OUTPUT_DIR})
  -n  File name prefix (default: ${NAME})
  -p  Password for the exported .p12 file (default: empty)
  -d  Validity in days for every generated certificate (default: ${DAYS})
  -h  Show this help
EOF
}

while getopts ":o:n:p:d:h" opt; do
	case "${opt}" in
	o) OUTPUT_DIR="${OPTARG}" ;;
	n) NAME="${OPTARG}" ;;
	p) P12_PASSWORD="${OPTARG}" ;;
	d) DAYS="${OPTARG}" ;;
	h)
		usage
		exit 0
		;;
	\?)
		echo "Invalid option: -${OPTARG}" >&2
		usage >&2
		exit 1
		;;
	:)
		echo "Option -${OPTARG} requires an argument." >&2
		usage >&2
		exit 1
		;;
	esac
done

if ! command -v openssl >/dev/null 2>&1; then
	echo "Error: openssl is not installed." >&2
	exit 1
fi

mkdir -p "${OUTPUT_DIR}"
cd "${OUTPUT_DIR}"

echo "==> Generating root CA certificate and private key"
openssl req -new -x509 -nodes -days "${DAYS}" \
	-subj "${ROOT_CA_SUBJECT}" \
	-out rootCA.crt -keyout rootCA.key

echo "==> Generating intermediate CA certificate and private key"
openssl req -new -nodes \
	-subj "${INTERMEDIATE_CA_SUBJECT}" \
	-keyout intermediateCA.key -out intermediateCA.csr

echo "==> Signing intermediate CA with root CA"
openssl x509 -req -days "${DAYS}" \
	-in intermediateCA.csr \
	-CA rootCA.crt -CAkey rootCA.key -CAcreateserial \
	-out intermediateCA.crt

echo "==> Generating leaf certificate and private key"
openssl req -new -nodes \
	-subj "${LEAF_SUBJECT}" \
	-keyout "${NAME}.key" -out "${NAME}.csr"

echo "==> Signing leaf certificate with intermediate CA"
openssl x509 -req -days "${DAYS}" \
	-in "${NAME}.csr" \
	-CA intermediateCA.crt -CAkey intermediateCA.key -CAcreateserial \
	-out "${NAME}.crt"

echo "==> Exporting ${NAME}.p12"
openssl pkcs12 -export \
	-out "${NAME}.p12" \
	-inkey "${NAME}.key" \
	-in "${NAME}.crt" \
	-certfile intermediateCA.crt \
	-CAfile rootCA.crt \
	-passout "pass:${P12_PASSWORD}"

echo "==> Cleaning up intermediate files"
rm -f intermediateCA.csr "${NAME}.csr" intermediateCA.srl rootCA.srl

echo
echo "Done. Files created in ${OUTPUT_DIR}:"
ls -1 "${NAME}.p12" "${NAME}.crt" "${NAME}.key" intermediateCA.crt intermediateCA.key rootCA.crt rootCA.key
