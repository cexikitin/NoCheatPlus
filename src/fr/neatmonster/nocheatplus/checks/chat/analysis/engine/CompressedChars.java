package fr.neatmonster.nocheatplus.checks.chat.analysis.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.neatmonster.nocheatplus.checks.chat.analysis.MessageLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.WordLetterCount;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.PrefixTree;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.PrefixTree.LookupEntry;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.PrefixTree.Node;
import fr.neatmonster.nocheatplus.checks.chat.analysis.ds.PrefixTree.NodeFactory;

public class CompressedChars extends AbstractWordProcessor{

	protected final PrefixTree<Character, Node<Character>> tree = new PrefixTree<Character, Node<Character>>(
			new NodeFactory<Character, Node<Character>>() {
				@Override
				public Node<Character> getNewNode() {
					return new Node<Character>();
				}
			});
	
	protected final int maxAdd;
	
	protected int added = 0;

	private final boolean sort;
	
	public CompressedChars(int maxAdd, boolean sort) {
		super("CompressedChars");
		this.maxAdd = maxAdd;
		this.sort = sort;
	}
	
	@Override
	public void start(MessageLetterCount message) {
		if (added + message.words.length > maxAdd) tree.clear();
	}

	@Override
	public float loop(long ts, int index, String key, WordLetterCount word) {
		final List<Character> letters = new ArrayList<Character>(word.counts.size());
		final List<Character> numbers = new ArrayList<Character>(word.counts.size());
		final List<Character> other = new ArrayList<Character>(word.counts.size());
		for (Character c : word.counts.keySet()){
			if (Character.isLetter(c)) letters.add(c);
			else if (Character.isDigit(c)) numbers.add(c);
			else other.add(c);
		}
		if (sort){
			Collections.sort(letters);
			Collections.sort(numbers);
			Collections.sort(other);
		}
		float score = 0;
		float div = 0;
		if (!letters.isEmpty()){
			score += getScore(letters);
			div += 1;
		}
		if (!numbers.isEmpty()){
			score += getScore(numbers);
			div += 1;
		}
		if (!other.isEmpty()){
			score += getScore(other);
			div += 1;
		}
		return (div == 0) ? 0 : score;
	}

	private float getScore(List<Character> chars) {
		final int len = chars.size();
		LookupEntry<Character, Node<Character>> entry = tree.lookup(chars, true);
		float score = (float) (entry.depth*entry.depth) / (float) (len*len) ;
		if (entry.depth == chars.size()){
			score += 0.2;
			if (entry.insertion.isEnd) score += 0.2;
		}
		return score;
	}

}