package model.ordine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OrdineDAOTest {

    private OrdineDAO dao;
    private DataSource dsMock;
    private Connection connMock;
    private Statement stmtMock;
    private PreparedStatement psMock;
    private ResultSet rsMock;

    @BeforeEach
    void setup() throws Exception {
        dsMock = mock(DataSource.class);
        connMock = mock(Connection.class);
        stmtMock = mock(Statement.class);
        psMock = mock(PreparedStatement.class);
        rsMock = mock(ResultSet.class);

        dao = new OrdineDAO(dsMock);

        when(dsMock.getConnection()).thenReturn(connMock);
        when(connMock.prepareStatement(anyString())).thenReturn(psMock);
        when(connMock.createStatement()).thenReturn(stmtMock);
        when(psMock.executeQuery()).thenReturn(rsMock);
    }

    // -------- Test doRetrieveByKey(Integer) --------

    // {id_valido, rs_con_riga, db_ok}
    @Test
    void doRetrieveByKey_idValido() throws Exception {
        when(rsMock.getInt("ID")).thenReturn(1);
        when(rsMock.getString("username")).thenReturn("mango");
        when(rsMock.getFloat("prezzoTotale")).thenReturn(100f);
        when(rsMock.getDate("dataConsegna")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rsMock.getDate("dataOrdine")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rsMock.getString("nomeConsegna")).thenReturn("Mario");
        when(rsMock.getString("cognomeConsegna")).thenReturn("Rossi");
        when(rsMock.getString("cap")).thenReturn("80000");
        when(rsMock.getString("via")).thenReturn("Via Mango");
        when(rsMock.getString("citta")).thenReturn("Casotto");

        OrdineBean o = dao.doRetrieveByKey(1);

        assertEquals(1, o.getID());
        assertEquals("mango", o.getUsername());
    }

    // {db_exception}
    @Test
    void doRetrieveByKey_dbException() throws Exception {
        when(dsMock.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> dao.doRetrieveByKey(1));
    }

    // -------- Test doRetrieveByKey(String) --------

    // {username_valido, rs_vuoto, db_ok}
    @Test
    void doRetrieveByUsername_vuoto() throws Exception {
        when(rsMock.next()).thenReturn(false);

        Collection<OrdineBean> res = dao.doRetrieveByKey("mango");

        verify(psMock).setString(1, "mango");
        assertTrue(res.isEmpty());
    }

    // {username_valido, una_riga, db_ok}
    @Test
    void doRetrieveByUsername_unaRiga() throws Exception {
        when(rsMock.next()).thenReturn(true, false);

        when(rsMock.getInt("ID")).thenReturn(1);
        when(rsMock.getString("username")).thenReturn("mango");
        when(rsMock.getFloat("prezzoTotale")).thenReturn(50f);
        when(rsMock.getDate("dataConsegna")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rsMock.getDate("dataOrdine")).thenReturn(Date.valueOf(LocalDate.now()));
        when(rsMock.getString("nomeConsegna")).thenReturn("Mario");
        when(rsMock.getString("cognomeConsegna")).thenReturn("Rossi");
        when(rsMock.getString("cap")).thenReturn("80000");
        when(rsMock.getString("via")).thenReturn("Via Mango");
        when(rsMock.getString("citta")).thenReturn("Casotto");

        Collection<OrdineBean> res = dao.doRetrieveByKey("mango");

        assertEquals(1, res.size());
        verify(psMock).setString(1, "mango");
    }

    // -------- Test doRetriveAll() --------

    // {order_valido, piu_righe, db_ok}
    @Test
    void doRetrieveAll_orderValido() throws Exception {
        when(rsMock.next()).thenReturn(true, true, false);

        Date today = Date.valueOf(LocalDate.now());

        when(rsMock.getInt("ID")).thenReturn(1, 2);
        when(rsMock.getString("username")).thenReturn("mango", "mango");
        when(rsMock.getFloat("prezzoTotale")).thenReturn(10f, 20f);
        when(rsMock.getDate("dataConsegna")).thenReturn(today, today);
        when(rsMock.getDate("dataOrdine")).thenReturn(today, today);
        when(rsMock.getString("nomeConsegna")).thenReturn("Mario", "Mario");
        when(rsMock.getString("cognomeConsegna")).thenReturn("Rossi", "Rossi");
        when(rsMock.getString("cap")).thenReturn("80000", "80000");
        when(rsMock.getString("via")).thenReturn("Via Mango", "Via Mango");
        when(rsMock.getString("citta")).thenReturn("Casotto", "Casotto");

        Collection<OrdineBean> res = dao.doRetriveAll("username");

        assertEquals(2, res.size());
        verify(connMock).prepareStatement(contains("ORDER BY username"));
        verify(psMock).close();
        verify(connMock).close();
    }

    // {order_valido, prepareStatement_exception}
    @Test
    void doRetrieveAll_prepareStatementException() throws Exception {
        when(dsMock.getConnection()).thenReturn(connMock);
        when(connMock.prepareStatement(anyString())).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> dao.doRetriveAll("username"));

        verify(connMock).close();
    }

    // {order_valido, getConnection_exception}
    @Test
    void doRetrieveAll_connectionException() throws Exception {
        when(dsMock.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> dao.doRetriveAll("username"));
    }


    // {order_invalido, db_ok}
    @Test
    void doRetrieveAll_orderInvalido() throws Exception {
        when(rsMock.next()).thenReturn(false);

        Collection<OrdineBean> res = dao.doRetriveAll("nonValido");

        assertTrue(res.isEmpty());
        verify(connMock).close();
        verify(psMock).close();
    }

    // -------- Test doSave() --------

    // {ordine_valido, db_ok}
    @Test
    void doSave_ok() throws Exception {
        OrdineBean o = new OrdineBean();
        o.setUsername("mango");
        o.setPrezzoTotale(99f);
        o.setNomeConsegna("Mario");
        o.setCognomeConsegna("Rossi");
        o.setCap("80000");
        o.setVia("Via Mango");
        o.setCitta("Casotto");

        dao.doSave(o);

        verify(psMock).setString(1, "mango");
        verify(psMock).setFloat(2, 99f);
        verify(psMock).executeUpdate();
    }

    // -------- Test getMaxID() --------

    // {db_ok}
    @Test
    void getMaxID_ok() throws Exception {
        when(rsMock.next()).thenReturn(true);
        when(rsMock.getInt("AUTO_INCREMENT")).thenReturn(8);

        assertEquals(8, dao.getMaxID());
    }
}
