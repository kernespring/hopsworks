/*
 * Copyright (C) 2013 - 2018, Logical Clocks AB and RISE SICS AB. All rights reserved
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this
 * software and associated documentation files (the "Software"), to deal in the Software
 * without restriction, including without limitation the rights to use, copy, modify, merge,
 * publish, distribute, sublicense, and/or sell copies of the Software, and to permit
 * persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS  OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL  THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR  OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package io.hops.hopsworks.common.dao.hdfs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import io.hops.hopsworks.common.hdfs.DistributedFileSystemOps;
import io.hops.hopsworks.common.hdfs.DistributedFsService;
import org.apache.hadoop.conf.Configuration;
import io.hops.hopsworks.common.dao.AbstractFacade;

@Stateless
public class HdfsLeDescriptorsFacade extends AbstractFacade<HdfsLeDescriptors> {

  @PersistenceContext(unitName = "kthfsPU")
  private EntityManager em;

  @EJB
  private DistributedFsService dfsService;
  
  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public HdfsLeDescriptorsFacade() {
    super(HdfsLeDescriptors.class);
  }

  /**
   * HdfsLeDescriptors.hostname returns the hostname + port for the Leader NN
   * (e.g., "127.0.0.1:8020")
   *
   * @return
   */
  public HdfsLeDescriptors findEndpoint() {
    try {
//            return em.createNamedQuery("HdfsLeDescriptors.findEndpoint", HdfsLeDescriptors.class).getSingleResult();
      List<HdfsLeDescriptors> res = em.createNamedQuery(
              "HdfsLeDescriptors.findEndpoint", HdfsLeDescriptors.class).
              getResultList();
      if (res.isEmpty()) {
        return null;
      } else {
        return res.get(0);
      }
    } catch (NoResultException e) {
      return null;
    }
  }

  /**
   *
   * @return "ip:port" for the first namenode found in the table.
   */
  public String getSingleEndpoint() {
    HdfsLeDescriptors hdfs = findEndpoint();
    if (hdfs == null) {
      return "";
    }
    return hdfs.getHostname();
  }

  /**
   * Get the currently active NameNode. Loops the NameNodes provided by the
   * hdfs_le_descriptors table.
   *
   * @return
   */
  public HdfsLeDescriptors getActiveNN() {
    try {
      List<HdfsLeDescriptors> res = em.createNamedQuery(
          "HdfsLeDescriptors.findEndpoint", HdfsLeDescriptors.class).
          getResultList();
    
      if (res.isEmpty()) {
        return null;
      } else {
        //Try to open a connection to NN
        Configuration conf = new Configuration();
        for (HdfsLeDescriptors hdfsLeDesc : res) {
          try {
            DistributedFileSystemOps dfso = dfsService.getDfsOps(
                new URI("hdfs://" + hdfsLeDesc.getHostname()));
            if (null != dfso) {
              return hdfsLeDesc;
            }
          } catch (URISyntaxException ex) {
            Logger.getLogger(HdfsLeDescriptorsFacade.class.getName()).
                log(Level.SEVERE, null, ex);
          }
        }
      }
    } catch (NoResultException e) {
      return null;
    }
    return null;
  }

}
