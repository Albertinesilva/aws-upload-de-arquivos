package com.devsuperior.dscatalog.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.FilenameUtils;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

/**
 * Serviço responsável pelo gerenciamento de uploads de arquivos
 * para o Amazon S3.
 * 
 * <p>
 * Este serviço abstrai a comunicação com o cliente AWS SDK,
 * centralizando a lógica de:
 * </p>
 * <ul>
 * <li>Geração de nome único para o arquivo</li>
 * <li>Definição de metadados</li>
 * <li>Envio do arquivo ao bucket configurado</li>
 * <li>Retorno da URL pública do objeto armazenado</li>
 * </ul>
 * 
 * <p>
 * O nome do bucket é configurado via propriedade:
 * <code>s3.bucket</code>.
 * </p>
 * 
 * @author Albert
 */
@Service
public class S3Service {

  /**
   * Logger responsável pelo registro de eventos do serviço.
   */
  private static final Logger LOG = LoggerFactory.getLogger(S3Service.class);

  /**
   * Cliente Amazon S3 injetado pelo Spring.
   * Responsável por executar operações contra o bucket configurado.
   */
  @Autowired
  private AmazonS3 s3client;

  /**
   * Nome do bucket S3 definido nas propriedades da aplicação.
   */
  @Value("${s3.bucket}")
  private String bucketName;

  /**
   * Realiza o upload de um arquivo recebido via requisição multipart.
   * 
   * <p>
   * Fluxo executado:
   * </p>
   * <ol>
   * <li>Obtém o nome original do arquivo</li>
   * <li>Extrai a extensão</li>
   * <li>Gera um nome único baseado no timestamp atual</li>
   * <li>Obtém InputStream e Content-Type</li>
   * <li>Delegação para o método interno de upload</li>
   * </ol>
   *
   * @param file arquivo enviado via {@link MultipartFile}
   * @return URL pública do arquivo armazenado no S3
   * @throws IllegalArgumentException caso ocorra erro na leitura do arquivo
   */
  public URL uploadFile(MultipartFile file) {
    try {

      String originalName = file.getOriginalFilename();
      LOG.info("Received file: {}", originalName);

      String extension = FilenameUtils.getExtension(originalName);
      LOG.info("File extension: {}", extension);

      // Gera nome único baseado em timestamp
      String fileName = Instant.now().toDate().getTime() + "." + extension;

      InputStream inputStream = file.getInputStream();
      String contentType = file.getContentType();

      return uploadFile(inputStream, fileName, contentType);

    } catch (IOException e) {
      throw new IllegalArgumentException("Error converting MultipartFile to File", e);
    }
  }

  /**
   * Executa efetivamente o upload do arquivo para o bucket S3.
   *
   * <p>
   * Define metadados como Content-Type e realiza a operação
   * de armazenamento através do método {@code putObject}.
   * </p>
   *
   * @param inputStream fluxo de dados do arquivo
   * @param fileName    nome final do arquivo no bucket
   * @param contentType tipo MIME do arquivo
   * @return URL pública do objeto armazenado
   */
  private URL uploadFile(InputStream inputStream, String fileName, String contentType) {

    ObjectMetadata meta = new ObjectMetadata();
    meta.setContentType(contentType);
    LOG.info("Upload start - File: {}, Content-Type: {}", fileName, contentType);

    s3client.putObject(bucketName, fileName, inputStream, meta);
    LOG.info("Upload completed - File: {}", fileName);

    return s3client.getUrl(bucketName, fileName);
  }
}