package JGBC.GBC;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.util.Scanner;
/**
 * Gerenciamente de banco de dados SQL em java, baseado na arquitetura MVC.
 *
 */
class BancoDeDados {
	private String ip;
	private String porta;
	private String nome;
	private String usuario;
	private String senha;
	
	public BancoDeDados(String ip, String porta, String nome, String usuario, String senha) {
		this.ip = ip;
		this.porta = porta;
		this.nome = nome;
		this.usuario = usuario;
		this.senha = senha;
	}
	public String getIp() {
		return ip;
	}
	public String getPorta() {
		return porta;
	}
	public String getNome() {
		return nome;
	}
	public String getUsuario() {
		return usuario;
	}
	public String getSenha() {
		return senha;
	}
}
class BdInterface {	
	private String ip;
	private String porta;
	private Connection conectar;
	private Statement estado;
	private ResultSet resultado;
	
	public BdInterface(String ip, String porta) {
		this.ip = ip;
		this.porta = porta;
	}
	
	public void conexao(String nome, String usuario, String senha) {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			conectar = DriverManager.getConnection("jdbc:mariadb://" + ip + ":" + porta + "/" + nome, usuario, senha);
			estado = conectar.createStatement();
		}
		catch(SQLException | ClassNotFoundException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Atenção", 0);
		}
	}
	public void execucao(String sql) {
		try {
			estado.executeUpdate(sql);
			estado = conectar.createStatement();
		}
		catch(SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Atenção", 0);
		}
	}
	public ResultSet consulta(String sql) {
		try {
			resultado = estado.executeQuery(sql);
		}
		catch(SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Atenção", 0);
		}
		
		return resultado;
	}
	public void desconectar() {
		try {
			estado.close();
			conectar.close();
		}
		catch(SQLException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Atenção", 0);
		}
	}
}
class OBC {
	BdInterface interfaceDeConexao;
	BancoDeDados BD;
	
	public OBC(BancoDeDados BD) {
		this.BD = BD;
		interfaceDeConexao = new BdInterface(BD.getIp(), BD.getPorta());
		interfaceDeConexao.conexao(BD.getNome(), BD.getUsuario(), BD.getSenha());
	}
	public void adicionar(String tabela, String colunas, String valores) {
		interfaceDeConexao.execucao("INSERT INTO " + tabela + "(" + colunas + ") " + "VALUES" + "(" + valores + ");");
	}
	public void remover(String tabela, String colunas, String valores) {
		interfaceDeConexao.execucao("DELETE FROM " + tabela + " WHERE" + colunas + " =" +  " " + valores + ";");
	}
	public void atualizar(String tabela, String coluna, float valor, String campo, String valores) {
		interfaceDeConexao.execucao("UPDATE "+tabela+" SET "+coluna + " = " + valor + " WHERE "+campo+" = "+valores+";");
	}
	public ResultSet consultar(String tabela, String colunas, String valores) {
		ResultSet resultado = interfaceDeConexao.consulta("SELECT * FROM " +tabela+ " WHERE " +colunas+ "=" +valores+ ";");
		return resultado;
	}
	public void fecharConexao() {
		interfaceDeConexao.desconectar();
	}
}
class Usuario {
	private long cpf;
	private String nome, sobrenome;
	float saldo;
	BancoDeDados acessoBd;
	OBC baseDeDados;
	
	public Usuario() {
		acessoBd = new BancoDeDados("localhost", "3306", "JOBC", "root", "314159");
		baseDeDados = new OBC(acessoBd);
	}
	public void adicionar() {
		String campus = "Cpf, Nome, Sobrenome, Saldo";
		String novasInformacoes = "'" + this.cpf + "," + this.nome + "," + this.sobrenome + "," + this.saldo;
		baseDeDados.adicionar("Usuario", campus, novasInformacoes);
	}
	public void remover(String campo, String valor) {
		baseDeDados.remover("Usuario", campo, valor);
		this.cpf = 0;
		this.nome = "";
		this.sobrenome = "";
		this.saldo = 0;
	}
	public void atualizar(String campo, String valor) {
		String novasInformacoes = "Cpf = " + this.cpf + ", Nome = " + this.nome + ", Sobrenome = " + this.sobrenome + ", Saldo =" + this.saldo;
		baseDeDados.atualizar("USUARIO", "Saldo", this.saldo, campo, valor);
	}
	public void consultar(String campo, String valor) {
			int limiteDaTabela = 0;
			ResultSet consulta = baseDeDados.consultar("USUARIO", campo, valor);
			try {
				while(consulta.next()) {
					if (limiteDaTabela < 1) {
						this.cpf = consulta.getLong("Cpf");
						this.nome = consulta.getString("Nome");
						this.sobrenome = consulta.getString("Sobrenome");
						this.saldo = consulta.getFloat("Saldo");
						limiteDaTabela ++;
					}
					else {
						limpar();
					}
					System.out.println("a");
				}
			}
			catch(SQLException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Atenção", 0);
				limpar();
			}
	}
	public void fecharConexao() {
		baseDeDados.fecharConexao();
	}
	public void setSaldo(float saldo) {
		this.saldo = saldo;
	}
	public long getCpf() {
		return cpf;
	}
	public String getNome() {
		return nome;
	}
	public String getSobrenome() {
		return sobrenome;
	}
	public float getSaldo() {
		return saldo;
	}
	public void limpar() {
		this.cpf = 0L;
		this.nome = "";
		this.sobrenome = "";
		this.saldo = 0;
	}
}
class SaqueGui implements ActionListener {
	private int largura = 320;
	private int comprimento = 260;
	private int larguraDosObjetos[] = {50, 110, 200, 85, 115};
	private int comprimentoDosObjetos[] = {50, 100, 25};
	private Usuario pessoa = new Usuario();
	
	private JFrame moldura;
	private JLabel[] labels = new JLabel[4];
	private JTextField[] fields = new JTextField[4];
	private JButton botaoVoltar, botaoSacar;
	
	public SaqueGui() {
		moldura = new JFrame("Sacar Saldo");
		moldura.setSize(this.largura, this.comprimento);
		moldura.setResizable(false);
		moldura.setLayout(new BorderLayout());
		moldura.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel painel = new JPanel();
		painel.setLayout(null);
		
		labels[0] = new JLabel("Cpf");
		labels[0].setBounds(14, 0, larguraDosObjetos[0], 50);
		
		labels[1] = new JLabel("Nome");
		labels[1].setBounds(14, 40, larguraDosObjetos[0], 50);
		
		labels[2] = new JLabel("Sobrenome");
		labels[2].setBounds(14, 80, larguraDosObjetos[1], 50);
		
		labels[3] = new JLabel("Saldo");
		labels[3].setBounds(14, 120, larguraDosObjetos[0], 50);
		
		fields[0] = new JTextField();
		fields[0].setBounds(98, 15, larguraDosObjetos[1], comprimentoDosObjetos[2]);
		
		fields[1] = new JTextField();
		fields[1].setBounds(98, 55, larguraDosObjetos[2], comprimentoDosObjetos[2]);
		
		fields[2] = new JTextField();
		fields[2].setBounds(98, 95, larguraDosObjetos[2], comprimentoDosObjetos[2]);
		
		fields[3] = new JTextField("0");
		fields[3].setBounds(98, 135, larguraDosObjetos[1], comprimentoDosObjetos[2]);
				
		botaoVoltar = new JButton("Voltar");
		botaoVoltar.setBounds(14, 180, larguraDosObjetos[3], comprimentoDosObjetos[2]);
		botaoVoltar.addActionListener(this);
		
		botaoSacar = new JButton("Sacar");
		botaoSacar.setBounds(215, 180, larguraDosObjetos[3], comprimentoDosObjetos[2]);
		botaoSacar.addActionListener(this);
		
		painel.add(labels[0]);
		painel.add(labels[1]);
		painel.add(labels[2]);
		painel.add(labels[3]);
		
		painel.add(fields[0]);
		painel.add(fields[1]);
		painel.add(fields[2]);
		painel.add(fields[3]);
		
		painel.add(botaoVoltar);
		painel.add(botaoSacar);
		
		moldura.add(painel);
		moldura.setVisible(true);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String Objeto = ((JButton) e.getSource()).getText();
		switch(Objeto) {
			case "Voltar":
				new caixaEletronico();
				moldura.dispose();
				break;
			
			case "Sacar":
				realizarSaque();
				break;
		}
	}
	public void realizarSaque() {
		String fieldsData[] = {fields[0].getText(), "\""+fields[1].getText()+"\"", "\""+fields[2].getText()+"\""};
		for (int i = 0;  fieldsData.length > i; i ++) {
			if (fields[i].getText().length() != 0) {
				pessoa.consultar(labels[i].getText(), fieldsData[i]);
				float saldo = Float.valueOf(fields[3].getText()).floatValue();
				saldo = pessoa.getSaldo() - saldo;
				pessoa.setSaldo(saldo);
				pessoa.atualizar(labels[i].getText(), fieldsData[i]);
				System.out.println(saldo);
				if (pessoa.getCpf() == 0L) {
					JOptionPane.showMessageDialog(moldura, "Cliente inexistente ou dados insuficientes", "Atenção", 1);
				}
				pessoa.limpar();
				break;
			}
		}
	}
}
class depositoGui implements ActionListener {
	private int largura = 320;
	private int comprimento = 260;
	private int larguraDosObjetos[] = {50, 110, 200, 85, 115};
	private int comprimentoDosObjetos[] = {50, 100, 25};
	private Usuario pessoa = new Usuario();
	
	private JFrame moldura;
	private JLabel[] labels = new JLabel[4];
	private JTextField[] fields = new JTextField[4];
	private JButton botaoVoltar, botaoDepositar;
	
	public depositoGui() {
		moldura = new JFrame("Depositar Saldo");
		moldura.setSize(this.largura, this.comprimento);
		moldura.setResizable(false);
		moldura.setLayout(new BorderLayout());
		moldura.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel painel = new JPanel();
		painel.setLayout(null);
		
		labels[0] = new JLabel("Cpf");
		labels[0].setBounds(14, 0, larguraDosObjetos[0], 50);
		
		labels[1] = new JLabel("Nome");
		labels[1].setBounds(14, 40, larguraDosObjetos[0], 50);
		
		labels[2] = new JLabel("Sobrenome");
		labels[2].setBounds(14, 80, larguraDosObjetos[1], 50);
		
		labels[3] = new JLabel("Saldo");
		labels[3].setBounds(14, 120, larguraDosObjetos[0], 50);
		
		fields[0] = new JTextField();
		fields[0].setBounds(98, 15, larguraDosObjetos[1], comprimentoDosObjetos[2]);
		
		fields[1] = new JTextField();
		fields[1].setBounds(98, 55, larguraDosObjetos[2], comprimentoDosObjetos[2]);
		
		fields[2] = new JTextField();
		fields[2].setBounds(98, 95, larguraDosObjetos[2], comprimentoDosObjetos[2]);
		
		fields[3] = new JTextField("0");
		fields[3].setBounds(98, 135, larguraDosObjetos[1], comprimentoDosObjetos[2]);
				
		botaoVoltar = new JButton("Voltar");
		botaoVoltar.setBounds(14, 180, larguraDosObjetos[3], comprimentoDosObjetos[2]);
		botaoVoltar.addActionListener(this);
		
		botaoDepositar = new JButton("Depositar");
		botaoDepositar.setBounds(185, 180, larguraDosObjetos[4], comprimentoDosObjetos[2]);
		botaoDepositar.addActionListener(this);
		
		painel.add(labels[0]);
		painel.add(labels[1]);
		painel.add(labels[2]);
		painel.add(labels[3]);
		
		painel.add(fields[0]);
		painel.add(fields[1]);
		painel.add(fields[2]);
		painel.add(fields[3]);
		
		painel.add(botaoVoltar);
		painel.add(botaoDepositar);
		
		moldura.add(painel);
		moldura.setVisible(true);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String Objeto = ((JButton) e.getSource()).getText();
		switch(Objeto) {
			case "Voltar":
				new caixaEletronico();
				moldura.dispose();
				break;
			
			case "Depositar":
				realizarDeposito();
				break;
		}
	}
	public void realizarDeposito() {
		String fieldsData[] = {fields[0].getText(), "\""+fields[1].getText()+"\"", "\""+fields[2].getText()+"\""};
		for (int i = 0;  fieldsData.length > i; i ++) {
			if (fields[i].getText().length() != 0) {
				pessoa.consultar(labels[i].getText(), fieldsData[i]);
				float saldo = Float.valueOf(fields[3].getText()).floatValue();
				saldo += pessoa.getSaldo();
				pessoa.setSaldo(saldo);
				pessoa.atualizar(labels[i].getText(), fieldsData[i]);
				if (pessoa.getCpf() != 0) {
					JOptionPane.showMessageDialog(moldura, "Operação realizada com sucesso", "Atenção", 1);
					pessoa.limpar();
				}
				else {
					JOptionPane.showMessageDialog(moldura, "Cliente inexistente ou dados insuficientes", "Atenção", 1);
					pessoa.limpar();
				}
				break;
			}
		}
	}
}
class consultaGui implements ActionListener {
	private int largura = 320;
	private int comprimento = 260;
	private int larguraDosObjetos[] = {50, 110, 200, 85};
	private int comprimentoDosObjetos[] = {50, 100, 25};
	private Usuario pessoa = new Usuario();
	
	private JFrame moldura;
	private JLabel[] labels = new JLabel[4];
	private JTextField[] fields = new JTextField[4];
	private JButton botaoVoltar, botaoBuscar;
	
	public consultaGui() {
		moldura = new JFrame("Saldo do usuario");
		moldura.setSize(this.largura, this.comprimento);
		moldura.setResizable(false);
		moldura.setLayout(new BorderLayout());
		moldura.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel painel = new JPanel();
		painel.setLayout(null);
		
		labels[0] = new JLabel("Cpf");
		labels[0].setBounds(14, 0, larguraDosObjetos[0], 50);
		
		labels[1] = new JLabel("Nome");
		labels[1].setBounds(14, 40, larguraDosObjetos[0], 50);
		
		labels[2] = new JLabel("Sobrenome");
		labels[2].setBounds(14, 80, larguraDosObjetos[1], 50);
		
		labels[3] = new JLabel("Saldo");
		labels[3].setBounds(14, 120, larguraDosObjetos[0], 50);
		
		fields[0] = new JTextField();
		fields[0].setBounds(98, 15, larguraDosObjetos[1], comprimentoDosObjetos[2]);
		
		fields[1] = new JTextField();
		fields[1].setBounds(98, 55, larguraDosObjetos[2], comprimentoDosObjetos[2]);
		
		fields[2] = new JTextField();
		fields[2].setBounds(98, 95, larguraDosObjetos[2], comprimentoDosObjetos[2]);
		
		fields[3] = new JTextField("0");
		fields[3].setBounds(98, 135, larguraDosObjetos[1], comprimentoDosObjetos[2]);
				
		botaoVoltar = new JButton("Voltar");
		botaoVoltar.setBounds(14, 180, larguraDosObjetos[3], comprimentoDosObjetos[2]);
		botaoVoltar.addActionListener(this);
		
		botaoBuscar = new JButton("Buscar");
		botaoBuscar.setBounds(204, 180, larguraDosObjetos[3], comprimentoDosObjetos[2]);
		botaoBuscar.addActionListener(this);
		
		painel.add(labels[0]);
		painel.add(labels[1]);
		painel.add(labels[2]);
		painel.add(labels[3]);
		
		painel.add(fields[0]);
		painel.add(fields[1]);
		painel.add(fields[2]);
		painel.add(fields[3]);
		
		painel.add(botaoVoltar);
		painel.add(botaoBuscar);
		
		moldura.add(painel);
		moldura.setVisible(true);
	}
	@Override
	public void actionPerformed(ActionEvent e) {
		String Objeto = ((JButton) e.getSource()).getText();
		switch(Objeto) {
			case "Voltar":
				new caixaEletronico();
				moldura.dispose();
				break;
			
			case "Buscar":
				consultar();
				break;
		}
	}
	public void consultar() {
		String fieldsData[] = {fields[0].getText(), "\""+fields[1].getText()+"\"", "\""+fields[2].getText()+"\""};
		for (int i = 0;  fieldsData.length > i; i ++) {
			if (fields[i].getText().length() != 0) {
				pessoa.consultar(labels[i].getText(), fieldsData[i]);
				System.out.println(fields[i].getText());
				String saldo = Float.toString(pessoa.getSaldo());
				if (pessoa.getCpf() != 0L) { 
					fields[3].setText(saldo);
					pessoa.limpar();
				}
				else {
					JOptionPane.showMessageDialog(moldura, "Cliente inexistente ou dados insuficientes", "Atenção", 1);
				}
				break;
			}
		}
	}
}
class caixaEletronico implements ActionListener {	
	private final  int largura = 640;
	private final  int comprimento = 330;
	private final  Color cor_de_fundo = new Color(24, 113, 172);
	private final  int comprimento_do_botao = 60;
	private final  int largura_do_botao = 120;
	private final  Color cor_do_botao = new Color(235, 247, 255);
	private final  int posX = 230;
	private final  int[] posY = {20, 120, 220};
	private JFrame moldura;
	
	public caixaEletronico () {
        moldura = new JFrame("Gerenciamento Bancario");
        moldura.setSize(largura, comprimento);
        moldura.setResizable(false);
        moldura.setLayout(new BorderLayout());
        moldura.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel painel = new JPanel();
        painel.setLayout(null);
        
        JButton consulta = new JButton("consulta");
        consulta.setBounds(posX, posY[0], largura_do_botao, comprimento_do_botao);
        consulta.setBackground(cor_do_botao);
        consulta.addActionListener(this);
        
        JButton saque = new JButton("retirada");
        saque.setBounds(posX, posY[1], largura_do_botao, comprimento_do_botao);
        saque.setBackground(cor_do_botao);
        saque.addActionListener(this);
        
        JButton deposito = new JButton("depósito");
        deposito.setBounds(posX, posY[2], largura_do_botao, comprimento_do_botao);
        deposito.setBackground(cor_do_botao);
        deposito.addActionListener(this);
        
        painel.add(consulta);
        painel.add(saque);
        painel.add(deposito);
        
        painel.setBackground(cor_de_fundo);
        
        moldura.add(painel, BorderLayout.CENTER);
        moldura.setVisible(true);
	}
	public void actionPerformed(ActionEvent e) {
		String Objeto = ((JButton) e.getSource()).getText();
		switch (Objeto) {
			case "consulta":
				new consultaGui();
				moldura.dispose();
				break;
			case "depósito":
				new depositoGui();
				moldura.dispose();
				break;
			case "retirada":
				moldura.dispose();
				new SaqueGui();
		}
	}
}
public class App  {
	private static long cpf;
	private static String nome;
	private static String sobrenome;
	private static float saldo;
	
    public static void main(String[] args) {
		caixaEletronico caixa = new caixaEletronico();
    }
}
