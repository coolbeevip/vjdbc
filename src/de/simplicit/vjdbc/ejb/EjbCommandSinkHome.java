package de.simplicit.vjdbc.ejb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;

public interface EjbCommandSinkHome extends EJBHome {
    EjbCommandSink create() throws CreateException, EJBException;
}
