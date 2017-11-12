/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.unibi.cebitec.bibigrid.meta;

import de.unibi.cebitec.bibigrid.model.exceptions.ConfigurationException;

/**
 *
 * @author jsteiner
 * @param <E> The Environment specific Instance E
 * @param <P> The Provider specific Instance P
 */
public interface CreateClusterEnvironment<E, P> {

    /**
     * Api specific implementation of creating or choosing an exisiting
     * Virtual Private Cloud.
     * @return Environment E
     * @throws ConfigurationException
     */
    public E createVPC()  throws ConfigurationException;

    /**
     * Api specific implementation of creating or choosing a Subnet.
     *
     * @return Environment E
     * @throws ConfigurationException
     */
    public E createSubnet() throws ConfigurationException;

    /**
     * Api specific implementation of creating or choosing a SecurityGroup.
     *
     * @return Environment E
     * @throws ConfigurationException
     */
    public E createSecurityGroup()  throws ConfigurationException;

    /**
     * Api specific implementation of creating or choosing a placement group.
     * Needs to be the <b>LAST</b> Environment configuration and returns an
     * CreateCluster implementing Instance to step to instance configuration.
     *
     * @return Provider P
     * @throws ConfigurationException
     */
    public P createPlacementGroup()  throws ConfigurationException;
}
