import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class TxHandler {
    UTXOPool pool;

    private static final double epsilon = 0.000001d;
    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        this.pool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        return allOutputsValid(tx) && allSignaturesValid(tx) && noUTXOClaimedTwice(tx) && allOutputsNonNegative(tx) && netPositve(tx);
    }

    private boolean allOutputsValid(Transaction tx) {
        for (Transaction.Input input : tx.getInputs()) {
            UTXO inputUtxo = new UTXO(input.prevTxHash, input.outputIndex);
            if (!this.pool.contains(inputUtxo)) {
                return false;
            }
        }
        return true;
    }

    private boolean allSignaturesValid(Transaction tx) {
        for (int i = 0; i < tx.numInputs(); i++) {
            Transaction.Input input = tx.getInputs().get(i);
            UTXO inputUtxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output output = this.pool.getTxOutput(inputUtxo);
            if (!Crypto.verifySignature(output.address, tx.getRawDataToSign(i), input.signature)) {
                return false;
            }
        }
        return true;
    }

    private boolean noUTXOClaimedTwice(Transaction tx) {
        Set<UTXO> claimedUTXOs = new HashSet<>();
        for (Transaction.Input input : tx.getInputs()) {
            UTXO inputUtxo = new UTXO(input.prevTxHash, input.outputIndex);
            if (claimedUTXOs.contains(inputUtxo)) {
                return false;
            }
            claimedUTXOs.add(inputUtxo);
        }
        return true;
    }

    private boolean allOutputsNonNegative(Transaction tx) {
        for (Transaction.Output output : tx.getOutputs()) {
            if (output.value < 0) {
                return false;
            }
        }
        return true;
    }

    /*
     * Checks whether the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    private boolean netPositve(Transaction tx) {
        double sumOfInputs = 0, sumOfOutputs = 0;

        for (Transaction.Input input : tx.getInputs()) {
            UTXO inputUtxo = new UTXO(input.prevTxHash, input.outputIndex);
            sumOfInputs += this.pool.getTxOutput(inputUtxo).value;
        }

        for (Transaction.Output output : tx.getOutputs()) {
            sumOfOutputs += output.value;
        }

        return sumOfInputs >= sumOfOutputs || Math.abs(sumOfInputs - sumOfOutputs) < TxHandler.epsilon;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        Transaction[] validTransactions = Arrays.stream(possibleTxs).filter(tx -> isValidTx(tx)).toArray(Transaction[]::new);
    }

}
