FROM mcr.microsoft.com/devcontainers/java:1-21-bookworm

# Remove repositório do Yarn pré-configurado na imagem base: a chave GPG
# não valida (NO_PUBKEY), o que quebra o apt-get update rodado pela
# instalação da feature docker-in-docker. Não usamos Yarn/Node neste projeto.
RUN rm -f /etc/apt/sources.list.d/yarn.list
